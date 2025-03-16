import { useRouter } from "next/router";
import Image from "next/image";
import React from "react";
import NIV from "@/components/niv";
import image4 from "../../../../public/assets/0bc1afa088fef2b0de1c399ea24be955.jpeg";

export default function UploadedResult() {
  const router = useRouter();
  const { data } = router.query;

  let parsedData = null;
  try {
    parsedData = data ? JSON.parse(data as string) : null;
  } catch (error) {
    console.error("Failed to parse data:", error);
  }

  console.log("Parsed Data:", parsedData);

  const predictions: string[] = parsedData?.prediction?.[0] || [];

  return (
    <main className="flex flex-col h-screen w-screen">
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>

      <div className="flex h-full">
        <div className="w-1/2 h-full">
          <Image
            src={image4}
            alt="Travel Destination"
            className="object-cover w-full h-full"
            priority
          />
        </div>

        <div className="w-1/2 h-full bg-white p-6 overflow-y-auto">
          <h3 className="text-black font-bold text-lg mt-10 mb-4">
            Recommended Agencies:
          </h3>
          {predictions.length > 0 ? (
            <ul className="list-disc pl-5 text-gray-700">
              {predictions.map((agency: string, index: number) => (
                <li key={index} className="mb-2">
                  {agency}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-700">No recommendations available</p>
          )}
        </div>
      </div>
    </main>
  );
}
