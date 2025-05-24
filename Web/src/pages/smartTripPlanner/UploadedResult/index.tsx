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

  const destinations = parsedData?.destinations || [];
  const totalDuration = parsedData?.totalDuration || "";
  const totalDistance = parsedData?.totalDistance || 0;
  const month = parsedData?.month || "";

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
          <div className="mb-6">
            <h1 className="text-black font-bold text-xl mb-2">
              Your Smart Trip Plan
            </h1>
            <div className="text-gray-600 mb-4">
              <p>Total Duration: {totalDuration}</p>
              <p>Total Distance: {totalDistance} km</p>
              <p>Travel Month: {month}</p>
            </div>
          </div>

          <div className="space-y-8">
            {destinations.length > 0 ? (
              destinations.map((destination: any, destIndex: number) => {
                const tripPlan = destination.prediction || "";
                const tripDays = tripPlan
                  ? tripPlan.match(/(Day\s\d+:[\s\S]*?)(?=Day\s\d+:|$)/g) || []
                  : [];

                return (
                  <div key={destIndex} className="border-b pb-6">
                    <h2 className="text-lg font-semibold mb-4">
                      Destination {destIndex + 1}
                    </h2>
                    {tripDays.length > 0 ? (
                      tripDays.map((day: string, dayIndex: number) => {
                        const lines: string[] = day.trim().split("\n");
                        return (
                          <div key={dayIndex} className="space-y-4 mb-4">
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
                );
              })
            ) : (
              <p className="text-red-500">No destinations available.</p>
            )}
          </div>
        </div>
      </div>
    </main>
  );
}
