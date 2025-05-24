import Image from "next/image";
import image1 from "../../public/assets/dd4b7999b53e85b3e6a463bb4651848e.jpeg";
import { useRouter } from "next/router";

export default function Home() {
  const router = useRouter();

  const handleClick = () => {
    router.push("/login");
  };

  return (
    <main className="bg-gradient-to-b from-blue-200 to-blue-400 min-h-screen flex flex-col items-center px-4">
      <div className="text-center mt-16">
        <h1 className="font-extrabold text-5xl md:text-6xl leading-tight drop-shadow-md">
          <span className="text-blue-900">EXPLORE</span>{" "}
          <span className="text-white">SRI LANKA</span>
        </h1>
        <h2 className="font-semibold text-2xl md:text-3xl text-blue-800 mt-2 tracking-wide drop-shadow-sm">
          TRAVEL.COM
        </h2>
      </div>

      <div className="relative w-full max-w-6xl h-[300px] md:h-[500px] mt-10 rounded-2xl overflow-hidden shadow-2xl">
        <Image
          className="object-cover"
          src={image1}
          alt="Sri Lanka Exploration"
          layout="fill"
        />

        <div className="absolute bottom-5 left-1/2 transform -translate-x-1/2 grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6 p-4 w-full max-w-4xl">
          {[
            "Suggest Destination",
            "Quick & Easy Trip Planner",
            "Signboard Reader",
            "More About Places",
          ].map((text, index) => (
            <div
              key={index}
              className="backdrop-blur-md bg-white/30 shadow-xl rounded-full px-6 py-3 text-center font-medium text-gray-900 transition-all duration-300 transform hover:scale-105 hover:bg-white/40"
            >
              {text}
            </div>
          ))}
        </div>
      </div>

      <div className="text-center mt-8 mb-10">
        <button
          className="bg-sky-800 text-white text-lg font-bold rounded-full px-10 py-3 shadow-lg hover:bg-white hover:text-blue-900 hover:shadow-2xl transition-all duration-300 transform hover:scale-110"
          onClick={handleClick}
        >
          LET'S GO
        </button>
      </div>
    </main>
  );
}