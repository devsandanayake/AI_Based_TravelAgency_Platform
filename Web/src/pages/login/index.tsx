// import React, { useState } from "react";
// import image3 from "../../../public/assets/63d50ed9da2623df7584e9edad1e3668.jpeg";
// import Image from "next/image";
// import { useRouter } from "next/navigation";
// import { auth, database } from "../../../firebaseconfig";
// import { signInWithEmailAndPassword } from "firebase/auth";
// import { ref, get } from "firebase/database";
// import Swal from "sweetalert2";

// function Login() {
//   const router = useRouter();
//   const [email, setEmail] = useState("");
//   const [password, setPassword] = useState("");

//   const handleLogin = async () => {
//     try {
//       console.log("Logging in with:", email, password);

//       const userCredential = await signInWithEmailAndPassword(auth, email, password);
//       const user = userCredential.user;

//       if (user) {
//         console.log("Authentication successful:", user);

//         const userRef = ref(database, `user/${user.uid}`);
//         const snapshot = await get(userRef);

//         if (snapshot.exists()) {
//           const userData = snapshot.val();

//           localStorage.setItem("user", userData.email);
//           localStorage.setItem("id", userData.id);

//           router.push("/home");
//         } else {
//           console.error("User data not found in database.");
//           Swal.fire("Error", "User data not found in database", "error");
//         }
//       }
//     } catch (error) {
//       console.error("Error logging in:", error);
//       Swal.fire("Error", "Invalid email or password", "error");
//     }
//   };

//   return (
//     <div className="flex h-screen overflow-hidden bg-gradient-to-b from-sky-400 to-sky-600">
//       <div className="w-1/2 h-full relative">
//         <Image
//           className="object-cover w-full h-full rounded-l-3xl"
//           src={image3}
//           alt="Login Image"
//         />
//       </div>

//       <div className="w-1/2 flex items-center justify-center h-full bg-white shadow-lg rounded-r-3xl p-8">
//         <div className="max-w-md w-full px-6 py-12">
//           <h2 className="text-4xl font-extrabold text-center text-sky-800">Login</h2>
//           <h5 className="text-center text-gray-600 font-medium mt-2 mb-6">Please enter your login details to sign in.</h5>

//           <div className="space-y-4">

//             {/* Email Input */}
//             <div className="relative">
//               <input
//                 id="email"
//                 name="email"
//                 type="email"
//                 autoComplete="email"
//                 required
//                 onChange={(e) => setEmail(e.target.value)}
//                 className="block w-full px-4 py-2 rounded-md border-2 border-sky-300 focus:ring-2 focus:ring-sky-500 focus:border-transparent text-gray-900 placeholder-transparent focus:outline-none font-[Poppins]"
//                 placeholder="Email Address"
//               />
//               <label
//                 htmlFor="email"
//                 className="absolute left-4 top-2 text-gray-500 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-gray-500 peer-focus:text-sky-600 font-[Poppins]"
//               >
//                 Email Address
//               </label>
//             </div>

//             {/* Password Input */}
//             <div className="relative mt-4">
//               <input
//                 id="password"
//                 name="password"
//                 type="password"
//                 autoComplete="current-password"
//                 required
//                 onChange={(e) => setPassword(e.target.value)}
//                 className="block w-full px-4 py-2 rounded-md border-2 border-sky-300 focus:ring-2 focus:ring-sky-500 focus:border-transparent text-gray-900 placeholder-transparent focus:outline-none font-[Poppins]"
//                 placeholder="Password"
//               />
//               <label
//                 htmlFor="password"
//                 className="absolute left-4 top-2 text-gray-500 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-gray-500 peer-focus:text-sky-600 font-[Poppins]"
//               >
//                 Password
//               </label>
//             </div>

//             {/* Login Button */}
//             <div className="pt-5">
//               <button
//                 type="submit"
//                 className="flex w-full justify-center rounded-md bg-sky-800 px-4 py-2 text-sm font-semibold text-white shadow-lg hover:bg-sky-700 focus:outline-none focus:ring-2 focus:ring-sky-400"
//                 onClick={handleLogin}
//               >
//                 Log In
//               </button>
//             </div>

//             {/* Sign Up Link */}
//             <p className="mt-6 text-sm font-medium text-gray-600">
//               Don't you have an account?{" "}
//               <a
//                 href="/signup/"
//                 className="font-semibold text-sky-600 hover:text-sky-500"
//               >
//                 Sign up
//               </a>
//             </p>
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// }

// export default Login;

import React, { useState } from "react";
import image3 from "../../../public/assets/4.jpg";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { auth, database } from "../../../firebaseconfig";
import { signInWithEmailAndPassword } from "firebase/auth";
import { ref, get } from "firebase/database";
import Swal from "sweetalert2";

function login() {
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
    <div className="flex h-screen overflow-hidden bg-sky-100">
      <div className="w-1/2 h-full">
        <Image
          className="object-cover w-full h-full"
          src={image3}
          alt="Login Image"
        />
      </div>

      <div className="w-1/2 flex items-center justify-center h-full shadow-lg rounded-lg bg-gradient-to-b from-blue-200 to-blue-400">
        <div className="max-w-md w-full px-6 py-12 bg-[rgba(255,255,255,0.6)] shadow-xl rounded-xl transform transition duration-500 hover:scale-105 hover:shadow-2xl">
          <h2 className="text-4xl font-bold text-sky-700 leading-9 tracking-tight text-center font-[Poppins] text-[40px]">
            Login
          </h2>
          <br />
          <h5 className="text-center text-sky-700 font-semibold text-[10px] leading-[15px] font-[Poppins]">
            Please enter your login details to sign in.
          </h5>

          <div className="mt-10">
            <div className="relative mt-2">
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                onChange={(e) => setEmail(e.target.value)}
                className="block w-full px-3 pt-5 pb-2 rounded-md border-0 text-sky-900 shadow-sm ring-1 ring-inset ring-gray-500 placeholder-transparent focus:ring-2 focus:ring-inset focus:ring-indigo-700 peer sm:text-sm sm:leading-6 font-[Poppins]"
                style={{ backgroundColor: "white" }}
                placeholder="Email Address"
              />
              <label
                htmlFor="email"
                className="absolute left-3 top-2 text-sky-700 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-sky-700 peer-focus:text-indigo-700 font-[Poppins]"
              >
                Email Address
              </label>
            </div>

            <div className="relative mt-6">
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                required
                onChange={(e) => setPassword(e.target.value)}
                className="block w-full px-3 pt-5 pb-2 rounded-md border-0 text-sky-900 shadow-sm ring-1 ring-inset ring-gray-500 placeholder-transparent focus:ring-2 focus:ring-inset focus:ring-indigo-600 peer sm:text-sm sm:leading-6 font-[Poppins]"
                style={{ backgroundColor: "white" }}
                placeholder="Password"
              />
              <label
                htmlFor="password"
                className="absolute left-3 top-2 text-sky-700 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-sky-400 peer-focus:text-indigo-600 font-[Poppins]"
              >
                Password
              </label>
            </div>

            <div className="pt-5">
              <button
                type="submit"
                className="flex w-full justify-center rounded-md bg-sky-800 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm  focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 font-[Poppins]"
                onClick={handleLogin}
              >
                Log In
              </button>
            </div>

            <p className="mt-10 text-sm font-semibold text-zinc-600 font-[Poppins]">
              Don't you have an account?{" "}
              <a
                href="/signup/"
                className="font-semibold leading-6 text-blue-700 hover:text-primary-content"
              >
                Sign up
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default login;
