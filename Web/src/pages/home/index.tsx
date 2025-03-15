import Image from "next/image";
import { Inter } from "next/font/google";
import NIV from "@/components/niv";
import Footer from "@/components/Footer";
import image3 from "../../../public/assets/home.jpeg";
import image4 from "../../../public/assets/home1.jpeg";
import image5 from "../../../public/assets/home2.jpeg";
import image6 from "../../../public/assets/home3.jpeg";
import image7 from "../../../public/assets/home4.jpeg";
import image8 from "../../../public/assets/homelogo1.png";
import image9 from "../../../public/assets/homelogo2.png";
import image10 from "../../../public/assets/homelogo3.png";
import image11 from "../../../public/assets/homelogo4.png";

export default function Home() {
  return (
    <main>
      <NIV />
      <div className="relative">
        <Image
          className="mx-auto w-screen h-[100vh] w-auto"
          src={image3}
          alt="Your Company"
        />

        <div className="absolute top-10 text-white font-poppins">
          <h1
            className="text-6xl font-bold text-blue-700"
            style={{ marginLeft: "70px" }}
          >
            EXPLORE
          </h1>
          <h2
            className="text-6xl font-bold text-black mt-7"
            style={{ marginLeft: "130px" }}
          >
            THE SRILANKA
          </h2>
        </div>
      </div>

      <div className="flex justify-center">
        <h2 className="text-6xl font-bold text-black mt-5 font-Public Sans">
          Our Services
        </h2>
      </div>

      <div className="grid grid-cols-4 gap-8 mt-10">
        <div className="relative text-center font-Ubuntu">
          <Image
            className="mx-auto w-[100px] h-auto"
            src={image8}
            alt="Recommended Travel Agencies"
          />
          <p
            className="absolute inset-0 flex items-center text-sm text-black font-Ubuntu font-semibold"
            style={{ marginLeft: "250px" }}
          >
            Recommended Travel Agencies
          </p>
        </div>

        <div className="relative text-center">
          <Image
            className="mx-auto w-[100px] h-auto"
            src={image9}
            alt="Suggest Destinations"
          />
          <p
            className="absolute inset-0 flex items-center  text-sm text-black font-Ubuntu font-semibold"
            style={{ marginLeft: "250px" }}
          >
            Suggest Destinations
          </p>
        </div>

        <div className="relative text-center">
          <Image
            className="mx-auto w-[100px] h-auto"
            src={image10}
            alt="Scan Signboard"
          />
          <p
            className="absolute inset-0 flex items-center  text-sm text-black font-Ubuntu font-semibold"
            style={{ marginLeft: "250px" }}
          >
            Scan Signboard
          </p>
        </div>

        <div className="relative text-center">
          <Image
            className="mx-auto w-[100px] h-auto"
            src={image11}
            alt="Location Based Information"
          />
          <p
            className="absolute inset-0 flex items-center text-sm text-black font-Ubuntu font-semibold"
            style={{ marginLeft: "250px" }}
          >
            Location Based Information
          </p>
        </div>
      </div>

      <br />
      <div className="relative">
        <Image
          className="mx-auto w-screen h-[85vh] w-auto"
          src={image4}
          alt="Your Company"
        />
        <div className="absolute top-16 text-white font-poppins">
          <h1
            className="text-2xl font-bold text-blue-700 tracking-widest"
            style={{ marginLeft: "130px" }}
          >
            Suggest Destination
          </h1>

          <h2
            className="text-2xl font-bold text-black mt-5"
            style={{ marginLeft: "95px" }}
          >
            Suggest destination places based
          </h2>
          <h2
            className="text-2xl font-bold text-black"
            style={{ marginLeft: "170px" }}
          >
            on your answers
          </h2>
          <button
            className="bg-blue-700 text-white text-lg font-semibold rounded-lg shadow-lg hover:bg-blue-800 transition mt-5"
            style={{
              width: "200px",
              height: "40px",
              borderRadius: "20px",
              marginLeft: "160px",
            }}
          >
            Go Now
          </button>
        </div>
      </div>

      <div className="relative">
        <Image
          className="mx-auto w-screen h-[85vh] w-auto"
          src={image5}
          alt="Your Company"
        />
        <div className="absolute top-16 right-10 text-white font-poppins">
          <h1
            className="text-2xl font-bold text-blue-700 tracking-widest"
            style={{ marginLeft: "10px" }}
          >
            Quick and easy trip planner
          </h1>
          <h2 className="text-2xl font-bold text-black mt-5">
            Recommend suitable travel agencies
          </h2>

          <button
            className="bg-blue-700 text-white text-lg font-semibold rounded-lg shadow-lg hover:bg-blue-800 transition mt-5"
            style={{
              width: "200px",
              height: "40px",
              borderRadius: "20px",
              marginLeft: "100px",
            }}
          >
            Go Now
          </button>
        </div>
      </div>

      <div className="relative">
        <Image
          className="mx-auto w-screen h-[85vh] w-auto"
          src={image6}
          alt="Your Company"
        />
        <div className="absolute top-16 text-white font-poppins">
          <h1
            className="text-2xl font-bold text-blue-700 tracking-widest"
            style={{ marginLeft: "150px" }}
          >
            Signboard reader
          </h1>

          <h2
            className="text-2xl font-bold text-black mt-5"
            style={{ marginLeft: "95px" }}
          >
            Scan signboard and convert to text
          </h2>
          <button
            className="bg-blue-700 text-white text-lg font-semibold rounded-lg shadow-lg hover:bg-blue-800 transition mt-5"
            style={{
              width: "200px",
              height: "40px",
              borderRadius: "20px",
              marginLeft: "160px",
            }}
          >
            Go Now
          </button>
        </div>
      </div>

      <div className="relative">
        <Image
          className="mx-auto w-screen h-[85vh] w-auto"
          src={image7}
          alt="Your Company"
        />
        <div className="absolute top-16 right-10 text-white font-poppins">
          <h1
            className="text-2xl font-bold text-blue-700 tracking-widest"
            style={{ marginLeft: "110px" }}
          >
            More About Places
          </h1>
          <h2 className="text-2xl font-bold text-black mt-5">
            Use AR to obtain information about places
          </h2>
          <button
            className="bg-blue-700 text-white text-lg font-semibold rounded-lg shadow-lg hover:bg-blue-800 transition mt-5"
            style={{
              width: "200px",
              height: "40px",
              borderRadius: "20px",
              marginLeft: "140px",
            }}
          >
            Go Now
          </button>
        </div>
      </div>

      <Footer />
    </main>
  );
}
