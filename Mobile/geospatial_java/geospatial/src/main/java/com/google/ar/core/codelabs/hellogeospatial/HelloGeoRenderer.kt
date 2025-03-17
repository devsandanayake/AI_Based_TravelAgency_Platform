/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.codelabs.hellogeospatial

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.core.codelabs.hellogeospatial.helpers.NearbySearch
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper
import com.google.ar.core.examples.java.common.samplerender.Framebuffer
import com.google.ar.core.examples.java.common.samplerender.IndexBuffer
import com.google.ar.core.examples.java.common.samplerender.Mesh
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.java.common.samplerender.Shader
import com.google.ar.core.examples.java.common.samplerender.Texture
import com.google.ar.core.examples.java.common.samplerender.VertexBuffer
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer

import com.google.ar.core.exceptions.CameraNotAvailableException

import org.json.JSONObject
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class HelloGeoRenderer(val activity: HelloGeoActivity) :
  SampleRender.Renderer, DefaultLifecycleObserver {
  //<editor-fold desc="ARCore initialization" defaultstate="collapsed">
  companion object {
    val TAG = "HelloGeoRenderer"

    private val Z_NEAR = 0.1f
    private val Z_FAR = 1000f
  }
  lateinit var placesClient: PlacesClient
  lateinit var backgroundRenderer: BackgroundRenderer
  lateinit var virtualSceneFramebuffer: Framebuffer
  var hasSetTextureNames = false

  // Virtual object (ARCore pawn)
  lateinit var virtualObjectMesh: Mesh
  lateinit var virtualObjectShader: Shader
  lateinit var virtualObjectTexture: Texture

  lateinit var plasceNameMesh: Mesh
  lateinit var plasceNameShader: Shader
  lateinit var plasceNameTexture: Texture

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  val modelMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val projectionMatrix = FloatArray(16)
  val modelViewMatrix = FloatArray(16) // view x model

  val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

  val session
    get() = activity.arCoreSessionHelper.session

  val displayRotationHelper = DisplayRotationHelper(activity)
  val trackingStateHelper = TrackingStateHelper(activity)

  init {
    // Initialize Places API
    Places.initialize(activity.applicationContext, activity.getString(R.string.GoogleCloudApiKey))
    placesClient = Places.createClient(activity)
  }

  override fun onResume(owner: LifecycleOwner) {
    displayRotationHelper.onResume()
    hasSetTextureNames = false
  }

  override fun onPause(owner: LifecycleOwner) {
    displayRotationHelper.onPause()
  }

  override fun onSurfaceCreated(render: SampleRender) {
    // Prepare the rendering objects.
    // This involves reading shaders and 3D model files, so may throw an IOException.
    try {
      backgroundRenderer = BackgroundRenderer(render)
      virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

      // Virtual object to render (Geospatial Marker)
      virtualObjectTexture =
        Texture.createFromAsset(
          render,
          "models/solid_red.png",
          Texture.WrapMode.CLAMP_TO_EDGE,
          Texture.ColorFormat.SRGB
        )

      virtualObjectMesh = Mesh.createFromAsset(render, "models/maps.obj");
      virtualObjectShader =
        Shader.createFromAssets(
          render,
          "shaders/ar_unlit_object.vert",
          "shaders/ar_unlit_object.frag",
          /*defines=*/ null)
          .setTexture("u_Texture", virtualObjectTexture)

      backgroundRenderer.setUseDepthVisualization(render, false)
      backgroundRenderer.setUseOcclusion(render, false)
    } catch (e: IOException) {
      Log.e(TAG, "Failed to read a required asset file", e)
      showError("Failed to read a required asset file: $e")
    }
  }

  override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
    displayRotationHelper.onSurfaceChanged(width, height)
    virtualSceneFramebuffer.resize(width, height)
  }
  //</editor-fold>












  override fun onDrawFrame(render: SampleRender) {
    val session = session ?: return

    //<editor-fold desc="ARCore frame boilerplate" defaultstate="collapsed">
    // Texture names should only be set once on a GL thread unless they change. This is done during
    // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
    // initialized during the execution of onSurfaceCreated.
    if (!hasSetTextureNames) {
      session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
      hasSetTextureNames = true
    }

    // -- Update per-frame state

    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session)

    // Obtain the current frame from ARSession. When the configuration is set to
    // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
    // camera framerate.
    val frame =
      try {
        session.update()
      } catch (e: CameraNotAvailableException) {
        Log.e(TAG, "Camera not available during onDrawFrame", e)
        showError("Camera not available. Try restarting the app.")
        return
      }

    val camera = frame.camera

    // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
    // used to draw the background camera image.
    backgroundRenderer.updateDisplayGeometry(frame)

    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
    trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

    // -- Draw background
    if (frame.timestamp != 0L) {
      // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
      // drawing possible leftover data from previous sessions if the texture is reused.
      backgroundRenderer.drawBackground(render)
    }

    // If not tracking, don't draw 3D objects.
    if (camera.trackingState == TrackingState.PAUSED) {
      return
    }

    // Get projection matrix.
    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

    // Get camera matrix and draw.
    camera.getViewMatrix(viewMatrix, 0)

    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)
    //</editor-fold>

    val earth = session.earth
    if (earth?.trackingState == TrackingState.TRACKING) {
      val cameraGeospatialPose = earth.cameraGeospatialPose
      activity.view.mapView?.updateMapPosition(
        latitude = cameraGeospatialPose.latitude,
        longitude = cameraGeospatialPose.longitude,
        heading = cameraGeospatialPose.heading
      )
    }

    // Draw the placed anchor, if it exists.

    earthAnchors.forEach { (anchor, name) ->
      val bitmap = generateBitmapForAnchor(name) // Custom function to create a bitmap
      render.renderCompassAtAnchor(anchor,bitmap)
      //render.renderCompassAtAnchor(anchor)
    }


    // Compose the virtual scene with the background.
    backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)
  }

  var earthAnchors: MutableList<Pair<Anchor, String>> = mutableListOf() // Store anchor with its name



  fun onMapClick(latLng: LatLng) {
    val earth = session?.earth ?: return
    if (earth.trackingState != TrackingState.TRACKING) {
      return
    }
    earthAnchors.clear()
    val cameraGeospatialPose = earth.cameraGeospatialPose
    val currentLat = cameraGeospatialPose.latitude
    val currentLng = cameraGeospatialPose.longitude
    Log.d("CurrentLocation", "Lat: $currentLat, Lng: $currentLng")

    val nearbySearch = NearbySearch(placesClient)
    nearbySearch.searchNearby(currentLat, currentLng, 1000)
      .thenAccept { places ->
        // Process the list of Place objects
        places.forEach { place ->
          // Log the Place ID and Name
          println("Place ID: ${place.id}")
          println("Place Name: ${place.name}")
          var placeName = place.name ?: "Unnamed Place"
          // Get the latitude and longitude of the place
          val placeLatLng = place.latLng
          placeLatLng?.let {
            val placeLatitude = it.latitude
            val placeLongitude = it.longitude
            // Log or process the latitude and longitude
            println("Place Latitude: $placeLatitude, Place Longitude: $placeLongitude")
            val distance = getDistanceInMeters(currentLat, currentLng, placeLatitude, placeLongitude)
            val formattedDistance = String.format("%.2f", distance)
            val altitude = earth.cameraGeospatialPose.altitude - 1 // Adjust altitude as needed
            val qx = 0f
            val qy = 0f
            val qz = 0f
            val qw = 1f
            placeName="$placeName: (${formattedDistance} m)";
            // Create the new anchor using the place's latitude and longitude
            val newAnchor = earth.createAnchor(placeLatitude, placeLongitude, altitude, qx, qy, qz, qw)
            earthAnchors.add(Pair(newAnchor, placeName)) // Add anchor with its name

            activity.view.mapView?.earthMarker?.apply {
              position = place.latLng
              isVisible = true
            }

          }
        }
      }
      .exceptionally { throwable ->
        // Handle any errors
        throwable.printStackTrace()
        null
      }
    // Get the nearest restaurants from the current location
    // getNearbyRestaurants(currentLat, currentLng)
    // Place the earth anchor at the same altitude as that of the camera to make it easier to view.
    val altitude = earth.cameraGeospatialPose.altitude - 1
    // The rotation quaternion of the anchor in the East-Up-South (EUS) coordinate system.
    val qx = 0f
    val qy = 0f
    val qz = 0f
    val qw = 1f

    // Create the new anchor
//    val newAnchor = earth.createAnchor(latLng.latitude, latLng.longitude, altitude, qx, qy, qz, qw)
//    earthAnchors.add(newAnchor) // Add it to the list

    // Add a map marker for each anchor
    activity.view.mapView?.earthMarker?.apply {
      position = latLng
      isVisible = true
    }
  }

  fun createTextureFromBitmap(render: SampleRender, bitmap: Bitmap): Texture {
    // Generate a texture handle
    val textureHandle = IntArray(1)
    GLES20.glGenTextures(1, textureHandle, 0)
    if (textureHandle[0] == 0) {
      throw RuntimeException("Error generating OpenGL texture handle.")
    }

    // Bind the texture
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

    // Set texture parameters
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

    // Load the bitmap data into the texture
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    // Recycle the bitmap, as it is no longer needed
    bitmap.recycle()

    // Create and return a Texture object
    return Texture(
      render,
      Texture.Target.TEXTURE_2D,
      Texture.WrapMode.CLAMP_TO_EDGE
    )
  }

  private fun SampleRender.renderTextAtAnchor(anchor: Anchor, name: String) {
    // Set up text rendering here
    // This could involve creating a 3D text mesh or using a UI overlay for the name.
    // For simplicity, let's assume you render it as a 3D mesh above the anchor.

    // Example: Offset slightly above the anchor to render the name
    val namePosition = anchor.pose.translation
    val textOffset = floatArrayOf(0f, 0.2f, 0f) // Position the name above the anchor
    Matrix.translateM(modelMatrix, 0, namePosition[0], namePosition[1], namePosition[2])
    Matrix.translateM(modelMatrix, 0, textOffset[0], textOffset[1], textOffset[2])

    // Use a similar process to render the 3D text or create a label above the anchor.
    // For now, you can just print out the name for debugging.
    Log.d(TAG, "Anchor Name: $name at Position: ${namePosition.joinToString()}")
  }

  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor) {
    // Get the current pose of the Anchor in world space. The Anchor pose is updated
    // during calls to session.update() as ARCore refines its estimate of the world.
    anchor.pose.toMatrix(modelMatrix, 0)

    // Calculate model/view/projection matrices
    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

    // Update shader properties and draw
    virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
    draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
  }


//  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor, bitmap: Bitmap) {
//    // Generate the texture from the bitmap using the provided function
//    val compassTexture = createTextureFromBitmap(this, bitmap)
//
//    // Bind the generated texture to the shader
//    virtualObjectShader.setTexture("u_Texture", compassTexture)
//
//    // Get the current pose of the Anchor in world space
//    anchor.pose.toMatrix(modelMatrix, 0)
//
//    // Calculate model/view/projection matrices
//    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
//    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
//
//    // Update shader properties with the model-view-projection matrix
//    virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
//
//    // Draw the object with the updated shader
//    draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
//  }

//  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor, bitmap: Bitmap) {
//    // Step 1: Generate a texture from the bitmap
//    val compassTexture = createTextureFromBitmap(this, bitmap)
//
//    // Step 2: Create a shader and bind the bitmap texture specifically for the compass
//    val compassShader = Shader.createFromAssets(
//      this,
//      "shaders/2d_text_vertex_shader.vert", // Vertex shader for a flat object
//      "shaders/2d_text_fragment_shader.frag", // Fragment shader for texture rendering
//      null
//    ).setTexture("u_Texture", compassTexture)
//
//    // Step 3: Get the current pose of the anchor in world space
//    anchor.pose.toMatrix(modelMatrix, 0)
//
//    // Step 4: Set up model/view/projection matrices
//    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
//    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
//
//    // Step 5: Update the shader with the model-view-projection matrix for the compass
//    compassShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
//
//    // Step 6: Create a flat mesh (like a 2D quad) to render the compass bitmap
//
//
//    // Step 7: Draw the compass mesh with its shader
//    draw(virtualObjectMesh, compassShader, virtualSceneFramebuffer)
//  }

  private fun SampleRender.renderCompassAtAnchor(anchor: Anchor, bitmap: Bitmap) {
    // Step 1: Generate a texture from the bitmap
    plasceNameTexture =
      Texture.createFromBitmap(
        this,
        bitmap,
        Texture.WrapMode.CLAMP_TO_EDGE,
        Texture.ColorFormat.SRGB
      )
    plasceNameMesh = Mesh.createFromBitmap(this, bitmap);
    // Step 2: Create a shader and bind the bitmap texture specifically for the compass
    val compassShader = Shader.createFromAssets(
      this,
      "shaders/ar_unlit_object.vert",
      "shaders/ar_unlit_object.frag",
      null
    )

    // Step 3: Get the current pose of the anchor in world space
    anchor.pose.toMatrix(modelMatrix, 0)

    // Step 4: Set up model/view/projection matrices
    Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

    // Step 5: Update the shader with the model-view-projection matrix for the compass
    compassShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)

    // Step 7: Draw the compass mesh with its shader
    draw(plasceNameMesh, compassShader, virtualSceneFramebuffer)
  }


  fun generateBitmapForAnchor(name: String): Bitmap {
    // Create a bitmap dynamically, e.g., with text or an image
    val bitmap = Bitmap.createBitmap(600, 200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
      color = Color.WHITE
      textSize = 40f
      textAlign = Paint.Align.CENTER
    }

    // Save the current canvas state
    canvas.save()

    // Rotate the canvas 180 degrees (around the center of the bitmap)
    canvas.rotate(180f, bitmap.width / 2f, bitmap.height / 2f)

    // Draw the text
    canvas.drawText(name, bitmap.width / 2f, bitmap.height / 2f, paint)

    // Restore the canvas to its previous state
    canvas.restore()

    return bitmap
  }

  fun getDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Radius of the Earth in meters
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaPhi = Math.toRadians(lat2 - lat1)
    val deltaLambda = Math.toRadians(lon2 - lon1)

    val a = sin(deltaPhi / 2).pow(2) +
            cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c // Distance in meters
  }


  private fun showError(errorMessage: String) =
    activity.view.snackbarHelper.showError(activity, errorMessage)
}
