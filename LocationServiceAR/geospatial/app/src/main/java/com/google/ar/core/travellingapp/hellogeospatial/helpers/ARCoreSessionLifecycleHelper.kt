
package com.google.ar.core.travellingapp.hellogeospatial.helpers

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException


class ARCoreSessionLifecycleHelper(
  val activity: Activity,
  val features: Set<Session.Feature> = setOf()
) : DefaultLifecycleObserver {
  var installRequested = false
  var session: Session? = null
    private set


  var exceptionCallback: ((Exception) -> Unit)? = null


  var beforeSessionResume: ((Session) -> Unit)? = null

  /**
   * Attempts to create a session. If Google Play Services for AR is not installed or not up to
   * date, request installation.
   *
   * @return null when the session cannot be created due to a lack of the CAMERA permission or when
   * Google Play Services for AR is not installed or up to date, or when session creation fails for
   * any reason. In the case of a failure, [exceptionCallback] is invoked with the failure
   * exception.
   */
  private fun tryCreateSession(): Session? {
    // The app must have been given the CAMERA permission. If we don't have it yet, request it.
    if (!GeoPermissionsHelper.hasGeoPermissions(activity)) {
      GeoPermissionsHelper.requestPermissions(activity)
      return null
    }

    return try {
      // Request installation if necessary.
      when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)!!) {
        ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
          installRequested = true
          // tryCreateSession will be called again, so we return null for now.
          return null
        }
        ArCoreApk.InstallStatus.INSTALLED -> {
          // Left empty; nothing needs to be done.
        }
      }

      // Create a session if Google Play Services for AR is installed and up to date.
      Session(activity, features)
    } catch (e: Exception) {
      exceptionCallback?.invoke(e)
      null
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    val session = this.session ?: tryCreateSession() ?: return
    try {
      beforeSessionResume?.invoke(session)
      session.resume()
      this.session = session
    } catch (e: CameraNotAvailableException) {
      exceptionCallback?.invoke(e)
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    session?.pause()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    // Explicitly close the ARCore session to release native resources.
    // Review the API reference for important considerations before calling close() in apps with
    // more complicated lifecycle requirements:
    // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
    session?.close()
    session = null
  }
}
