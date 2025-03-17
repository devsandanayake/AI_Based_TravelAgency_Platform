import Image from "next/image";
import { useRouter } from "next/router";
import { FaMapMarkerAlt, FaRoute, FaSign, FaInfoCircle } from "react-icons/fa";

export default function Home() {
  const router = useRouter();

  const handleClick = () => {
    router.push("/login");
  };

  return (
    <main className="bg-gradient-to-b from-blue-200 to-blue-400 min-h-screen flex flex-col items-center px-4 relative">
      {/* Background Video */}
      <video
        className="object-cover w-full h-full absolute top-0 left-0 z-0"
        src="/assets/main.mp4"
        autoPlay
        loop
        muted
      />

      {/* Header */}
      <div className="text-center mt-16 z-10">
        <h1 className="font-extrabold text-5xl md:text-6xl leading-tight">
          <span className="text-blue-900" style={{ WebkitTextStroke: "1px white" }}>
            EXPLORE
          </span>{" "}
          <span className="text-white" style={{ WebkitTextStroke: "1px blue" }}>
            SRI LANKA
          </span>
        </h1>
      </div>

      {/* Feature Section - Centered in Page */}
      <div className="relative w-full max-w-6xl flex-grow flex items-center justify-center z-10">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 px-6">
          {[
            { text: "Suggest Destination", icon: <FaMapMarkerAlt /> },
            { text: "Quick & Easy Trip Planner", icon: <FaRoute /> },
            { text: "Signboard Reader", icon: <FaSign /> },
            { text: "More About Places", icon: <FaInfoCircle /> },
          ].map((item, index) => (
            <div
              key={index}
              className="backdrop-blur-sm bg-white/30 shadow-xl rounded-2xl p-10 flex flex-col items-center text-center text-gray-900 transition-all duration-300 transform hover:scale-105 hover:bg-white/40 w-64 h-64"
            >
              <div className="text-4xl text-blue-900 mb-4">{item.icon}</div>
              <p className="text-lg font-semibold">{item.text}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Call to Action - Bottom Right Corner */}
      <button
        className="bg-sky-800 text-white text-lg font-bold rounded-full px-12 py-4 shadow-lg hover:bg-white hover:text-blue-900 hover:shadow-2xl transition-all duration-300 transform hover:scale-110 absolute bottom-8 right-8 z-10"
        onClick={handleClick}
      >
        Explore
      </button>
    </main>
  );
}
