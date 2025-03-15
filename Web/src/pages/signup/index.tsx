import React, { useState } from "react";
import image3 from "../../../public/assets/2.jpg";
import Image from "next/image";
import { useRouter } from "next/navigation";
import { app, auth, database } from "../../../firebaseconfig";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { ref, push, set } from "firebase/database";
import Swal from "sweetalert2";

function Signup() {
  const router = useRouter();
  const [type, setType] = useState(true);
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [cpassword, setCpassword] = useState<string>("");
  const [name, setName] = useState<string>("");

  const checkPassword = (value: string) => {
    setCpassword(value);
    setType(password === value);
  };

  const handleSignup = async () => {
    Swal.fire({
      title: "Processing...",
      html: 'Please wait while the data is being processed.<br><div class="spinner-border" role="status"></div>',
      allowOutsideClick: false,
      showCancelButton: false,
      showConfirmButton: false,
    });

    if (type) {
      try {
        const userCredential = await createUserWithEmailAndPassword(
          auth,
          email,
          password
        );
        const user = userCredential.user;
        console.log("User signed up:", user);

        const values = {
          email: email,
          uname: name,
          uid: user?.uid,
        };

        const userRef = ref(database, `user/${user.uid}`);
        await set(userRef, values);

        Swal.fire("Success", "User added successfully", "success");
        localStorage.setItem("user", values.email);
        localStorage.setItem("id", user.uid);
        router.push("/home");
      } catch (error) {
        Swal.fire("Error", "Email is already used or invalid", "error");
        console.error("Error signing up:", error);
      }
    } else {
      Swal.fire("Error", "Passwords do not match", "error");
    }
  };

  return (
    <>
      <div className="flex h-screen overflow-hidden bg-sky-100">
        <div className="w-full bg-white shadow-md z-10">
          <Image
            className="object-cover w-full h-full"
            src={image3}
            alt="Signup Image"
          />
        </div>
        <div className="w-1/2 flex items-center justify-center h-full shadow-lg rounded-lg bg-gradient-to-b from-blue-200 to-blue-400">
          <div className="max-w-md w-full px-6 py-12 bg-[rgba(255,255,255,0.6)] shadow-xl rounded-xl transform transition duration-500 hover:scale-105 hover:shadow-2xl">
            <h2 className="text-4xl font-bold text-sky-700 leading-9 tracking-tight text-center font-[Poppins] text-[40px]">
              Sign Up
            </h2>
            <br />
            <h5 className="text-center text-sky-700 font-semibold text-[10px] leading-[15px] font-[Poppins]">
              Create your account in seconds
            </h5>

            <div className="mt-10">
              <div className="relative mt-2">
                <input
                  id="name"
                  name="name"
                  type="text"
                  autoComplete="name"
                  required
                  onChange={(e) => setName(e.target.value)}
                  className="block w-full px-3 pt-5 pb-2 rounded-md border-0 text-sky-900 shadow-sm ring-1 ring-inset ring-gray-500 placeholder-transparent focus:ring-2 focus:ring-inset focus:ring-indigo-700 peer sm:text-sm sm:leading-6 font-[Poppins]"
                  style={{ backgroundColor: "white" }}
                  placeholder="Name"
                />
                <label
                  htmlFor="name"
                  className="absolute left-3 top-2 text-sky-700 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-sky-700 peer-focus:text-indigo-700 font-[Poppins]"
                >
                  Name
                </label>
              </div>

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
              </div>

              <div className="mt-10">
                <div className="relative mt-2">
                  <input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    required
                    onChange={(e) => setPassword(e.target.value)}
                    className="block w-full px-3 pt-5 pb-2 rounded-md border-0 text-sky-900 shadow-sm ring-1 ring-inset ring-gray-500 placeholder-transparent focus:ring-2 focus:ring-inset focus:ring-indigo-600 peer sm:text-sm sm:leading-6 font-[Poppins]"
                    placeholder="Password"
                  />
                  <label
                    htmlFor="password"
                    className="absolute left-3 top-2 text-sky-700 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-sky-700 peer-focus:text-indigo-600 font-[Poppins]"
                  >
                    Password
                  </label>
                </div>
              </div>

              <div className="mt-10">
                <div className="relative mt-2">
                  <input
                    id="cpassword"
                    name="cpassword"
                    type="password"
                    autoComplete="current-password"
                    required
                    onChange={(e) => checkPassword(e.target.value)}
                    className="block w-full px-3 pt-5 pb-2 rounded-md border-0 text-sky-700 shadow-sm ring-1 ring-inset ring-gray-300 placeholder-transparent focus:ring-2 focus:ring-inset focus:ring-indigo-600 peer sm:text-sm sm:leading-6 font-[Poppins]"
                    placeholder="Confirm Password"
                  />
                  <label
                    htmlFor="cpassword"
                    className="absolute left-3 top-2 text-sky-700 text-xs transition-all peer-focus:top-2 peer-focus:text-xs peer-placeholder-shown:top-5 peer-placeholder-shown:text-sm peer-placeholder-shown:text-sky-700 peer-focus:text-indigo-600 font-[Poppins]"
                  >
                    Confirm Password
                  </label>
                </div>
                <div
                  id="form-text"
                  hidden={type}
                  className="text-red-500 font-[Poppins]"
                >
                  Passwords do not match.
                </div>
              </div>

              <div className="pt-5">
                <button
                  type="submit"
                  className="flex w-full justify-center rounded-md bg-sky-800 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm  focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 font-[Poppins]"
                  onClick={handleSignup}
                >
                  Create an account
                </button>
              </div>

              <p className="mt-10 text-sm font-semibold text-zinc-600 font-[Poppins]">
                Already a member?
                <a
                  href="/login"
                  className="font-semibold leading-6 text-blue-700 hover:text-primary-content"
                >
                  Log in
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Signup;
