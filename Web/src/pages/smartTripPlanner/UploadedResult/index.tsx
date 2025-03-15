import { useRouter } from "next/router";
import Image from "next/image";
import React from "react";
import NIV from "@/components/niv";
import image4 from "../../../../public/assets/0bc1afa088fef2b0de1c399ea24be955.jpeg";

export default function UploadedResult() {
  const router = useRouter();
  const { data } = router.query;
  const parsedData = data ? JSON.parse(data as string) : null;
  console.log("parsedData", parsedData);

  const recommendedTripPlan = parsedData?.prediction || "";
  console.log("recommendedTripPlan", recommendedTripPlan);

  const tripDays = recommendedTripPlan
    ? recommendedTripPlan.match(/(Day\s\d+:[\s\S]*?)(?=Day\s\d+:|$)/g) || []
    : [];

  console.log("tripDays", tripDays);

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
          <h1 className="text-black font-bold text-xl mb-6">
            Here is your smart trip plan. Enjoy your trip!
          </h1>
          <div className="space-y-8">
            {tripDays.length > 0 ? (
              tripDays.map((day: string, index: number) => {
                const lines: string[] = day.trim().split("\n");
                return (
                  <div key={index} className="space-y-4">
                    {lines.map((line: string, i: number) => (
                      <p key={i} className="text-gray-700">
                        {line.trim()}
                      </p>
                    ))}
                  </div>
                );
              })
            ) : (
              <p className="text-red-500">No trip details available.</p>
            )}
          </div>
        </div>
      </div>
    </main>
  );
}
