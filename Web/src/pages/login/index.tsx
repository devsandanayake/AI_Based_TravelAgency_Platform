import React, { useState } from "react";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { auth, database } from "../../../firebaseconfig";
import { signInWithEmailAndPassword } from "firebase/auth";
import { ref, get } from "firebase/database";
import Swal from "sweetalert2";

import image3 from "../../../public/assets/4.jpg";

export default function Login() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async () => {
    try {
      console.log("Logging in with:", email, password);

      const userCredential = await signInWithEmailAndPassword(
        auth,
        email,
        password
      );
      const user = userCredential.user;

      if (user) {
        console.log("Authentication successful:", user);

        const userRef = ref(database, `user/${user.uid}`);
        const snapshot = await get(userRef);

        if (snapshot.exists()) {
          const userData = snapshot.val();

          localStorage.setItem("user", userData.email);
          console.log("User data saved to localStorage:", userData.email);
          localStorage.setItem("id", userData.id);

          console.log("User data saved to localStorage:", userData);

          router.push("/home");
        } else {
          console.error("User data not found in database.");
          Swal.fire("Error", "User data not found in database", "error");
        }
      }
    } catch (error) {
      console.error("Error logging in:", error);
      Swal.fire("Error", "Invalid email or password", "error");
    }
  };

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Left Side Image */}
      <div className="w-1/2 hidden lg:block">
        <Image className="object-cover w-full h-full" src={image3} alt="Login Image" />
      </div>

      {/* Right Side Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center bg-gradient-to-b from-blue-200 to-blue-400 px-8">
        <div className="max-w-md w-full px-8 py-12 bg-white/30 backdrop-blur-md shadow-xl rounded-2xl transition duration-500 hover:scale-105 hover:shadow-2xl">
          {/* Title */}
          <h2 className="text-4xl font-extrabold text-blue-800 text-center mb-4">
            Welcome Back
          </h2>
          <p className="text-center text-gray-700 font-medium mb-8">
            Log in to continue your journey!
          </p>

          {/* Input Fields */}
          <div className="space-y-6">
            <div className="relative">
              <input
                id="email"
                name="email"
                type="email"
                required
                className="block w-full px-4 pt-5 pb-2 text-gray-900 bg-white rounded-md shadow-md focus:ring-2 focus:ring-indigo-500 focus:outline-none peer"
                onChange={(e) => setEmail(e.target.value)}
                placeholder=" "
              />
              <label
                htmlFor="email"
                className="absolute left-4 top-2 text-gray-700 text-sm transition-all peer-placeholder-shown:top-5 peer-placeholder-shown:text-base peer-placeholder-shown:text-gray-400 peer-focus:top-2 peer-focus:text-sm peer-focus:text-indigo-700"
              >
                Email Address
              </label>
            </div>

            <div className="relative">
              <input
                id="password"
                name="password"
                type="password"
                required
                className="block w-full px-4 pt-5 pb-2 text-gray-900 bg-white rounded-md shadow-md focus:ring-2 focus:ring-indigo-500 focus:outline-none peer"
                onChange={(e) => setPassword(e.target.value)}
                placeholder=" "
              />
              <label
                htmlFor="password"
                className="absolute left-4 top-2 text-gray-700 text-sm transition-all peer-placeholder-shown:top-5 peer-placeholder-shown:text-base peer-placeholder-shown:text-gray-400 peer-focus:top-2 peer-focus:text-sm peer-focus:text-indigo-700"
              >
                Password
              </label>
            </div>
          </div>

          {/* Forgot Password & Login Button */}
          <div className="flex justify-between items-center mt-6">
            <a href="#" className="text-blue-700 hover:underline text-sm">
              Forgot password?
            </a>
          </div>

          <button
            className="mt-6 w-full bg-blue-800 text-white font-semibold py-3 rounded-md shadow-lg transition-all hover:bg-white hover:text-blue-900 hover:shadow-xl transform hover:scale-105"
            onClick={handleLogin}
          >
            Log In
          </button>

          {/* Signup Redirect */}
          <p className="mt-6 text-center text-gray-700 text-sm">
            Don't have an account?{" "}
            <a href="/signup" className="text-blue-700 font-semibold hover:underline">
              Sign up
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}