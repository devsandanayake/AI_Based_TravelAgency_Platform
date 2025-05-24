import Link from "next/link";
import Router from "next/router";
import React, { useEffect, useState } from "react";
import Swal from "sweetalert2";
import Image from "next/image";
import { usePathname } from "next/navigation"; 
import image1 from "../../public/assets/6.png";

export default function Niv() {
  const [email, setEmail] = useState<string | null>(null);
  const pathname = usePathname(); 

  useEffect(() => {
    const storedEmail = localStorage.getItem("user");
    if (storedEmail) {
      setEmail(storedEmail);
    }
  }, []);

  const handleLogout = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (email) {
      const result = await Swal.fire({
        title: "Are you sure?",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, Log out",
      });
      if (result.isConfirmed) {
        try {
          sessionStorage.removeItem("user");
          Router.push("/login");
        } catch (error) {
          console.error("Error during handleLogout: ", error);
          alert("An error occurred. Please try again later.");
        }
      }
    }
  };

  const linkClass = (path: string) =>
    `px-4 py-2 text-lg font-bold rounded ${
      pathname === path
        ? "bg-white text-blue-800" 
        : "hover:bg-blue-700 hover:text-white"
    }`;

  return (
    <div className="navbar top-0 left-0 right-0 bg-blue-800 text-white z-50">
      <div className="navbar-start flex items-center">
        <Link href="/home">
          <Image
            src={image1}
            alt="Logo"
            className="ml-2 w-36 h-10 cursor-pointer"
          />
        </Link>

        <div className="dropdown lg:hidden ml-4">
          <div
            tabIndex={0}
            role="button"
            className="btn btn-ghost"
            aria-label="Menu"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M4 6h16M4 12h8m-8 6h16"
              />
            </svg>
          </div>
          <ul
            tabIndex={0}
            className="menu menu-sm dropdown-content mt-3 w-52 bg-blue-800 rounded-box shadow-lg"
          >
            <li>
              <Link href="/home"><b>Home</b></Link>
            </li>
            <li>
              <Link href="/smartTripPlanner"><b>Smart Trip Planner</b></Link>
            </li>
            <li>
              <Link href="/smartTripPlanner/UploadedResult"><b>Uploaded Result</b></Link>
            </li>
            <li>
              <Link href="/smartTripPlanner/AllResults"><b>All Results</b></Link>
            </li>
            <li>
              <Link href="/agencies"><b>Agencies</b></Link>
            </li>
            <li>
              <Link href="/agencies/UploadedResult"><b>Uploaded Result</b></Link>
            </li>
            <li>
              <Link href="/agencies/AllResults"><b>All Results</b></Link>
            </li>
            <li>
              <Link href="/agencies/Feedback"><b>Feedback</b></Link>
            </li>
            <li>
            <Link href="/requests"><b>Feedback</b></Link>
            </li>
          </ul>
        </div>
      </div>

      <div className="navbar-center hidden lg:flex">
        {email && (
          <ul className="menu menu-horizontal px-1">
            <li>
              <Link href="/home" className={linkClass("/home")}>Home</Link>
            </li>
            <li>
              <Link href="/smartTripPlanner" className={linkClass("/smartTripPlanner")}>Smart Trip Planner</Link>
            </li>
            <li>
              <Link href="/agencies" className={linkClass("/agencies")}>Agencies</Link>
            </li>
            <li>
            <Link href="/requests" className={linkClass("/requests")}>Requests</Link>
            </li>
          </ul>
        )}
      </div>

      <div className="navbar-end">
        {email && (
          <Link
            href="/login"
            onClick={handleLogout}
            className="px-4 py-2 text-lg font-bold bg-blue-600 hover:bg-blue-700 text-white rounded"
          >
            Log out
          </Link>
        )}
      </div>
    </div>
  );
}
