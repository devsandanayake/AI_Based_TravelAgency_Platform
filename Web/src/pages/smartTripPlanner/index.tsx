import Image from "next/image";
import { useState, useEffect } from "react";
import axios from "axios";
import { ref, push, set } from "firebase/database";
import { app, database } from "../../../firebaseconfig";
import NIV from "@/components/niv";
import image3 from "../../../public/assets/6.jpg";
import Swal from "sweetalert2";
import { useRouter } from "next/router";
import image0 from "../../../public/assets/polgaha.png";
import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI("AIzaSyClrqxULSoLp_5q155EJpyZxCfOUPyKnrY");

interface DestinationData {
  name: string;
  days: string;
  activities: string;
  specialRequirements: string;
  coordinates?: { lat: number; lng: number };
  climate?: string;
}

interface DistanceMatrix {
  [key: string]: {
    [key: string]: number;
  };
}

const MAX_DAILY_DISTANCE = 150;

export default function Destination() {
  const [formData, setFormData] = useState({
    destinations: [] as DestinationData[],
    month: "",
  });

  const destinations = [
    "Trincomalee, Sri Lanka",
    "Nuwara Eliya, Sri Lanka",
    "Mirissa, Sri Lanka",
    "Colombo, Sri Lanka ",
    "Kandy, Sri Lanka",
    "Jaffna, Sri Lanka",
    "Ella, Sri Lanka",
    "Galle, Sri Lanka",
    "Anuradhapura, Sri Lanka",
    "Mannar, Sri Lanka",
    "Katharagama, Sri Lanka",
    "Polonnaruwa, Sri Lanka",
  ];

  const router = useRouter();
  const [showOptions, setShowOptions] = useState(false);
  const [selectedDestinations, setSelectedDestinations] = useState<string[]>(
    []
  );
  const [destinationData, setDestinationData] = useState<{
    [key: string]: DestinationData;
  }>({});
  const [distanceMatrix, setDistanceMatrix] = useState<DistanceMatrix>({});
  const [isValidRoute, setIsValidRoute] = useState(true);

  const geocodeLocation = async (location: string) => {
    try {
      console.log("Geocoding location:", location);
      const response = await axios.get(
        `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(
          location
        )}&key=AIzaSyAK-DQ333t4oIpyiWmrKQSxXhwbULsEjno`
      );

      console.log("Geocoding response:", response.data);

      if (response.data.status === "ZERO_RESULTS") {
        throw new Error(`No results found for location: ${location}`);
      }

      if (response.data.status !== "OK") {
        throw new Error(
          `Geocoding failed with status: ${response.data.status}`
        );
      }

      if (response.data.results.length > 0) {
        const { lat, lng } = response.data.results[0].geometry.location;
        console.log(`Coordinates for ${location}:`, { lat, lng });
        return { lat, lng };
      }

      throw new Error(`No coordinates found for location: ${location}`);
    } catch (error: any) {
      console.error("Geocoding error details:", error);
      throw new Error(
        `Failed to geocode location "${location}": ${error.message}`
      );
    }
  };

  const calculateDistance = (
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number
  ) => {
    const R = 6371;
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLon = ((lon2 - lon1) * Math.PI) / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos((lat1 * Math.PI) / 180) *
        Math.cos((lat2 * Math.PI) / 180) *
        Math.sin(dLon / 2) *
        Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  };

  useEffect(() => {
    if (selectedDestinations.length < 2) return;

    const calculateDistances = async () => {
      try {
        const newDistanceMatrix: DistanceMatrix = {};
        const coordinates: { [key: string]: { lat: number; lng: number } } = {};

        for (const dest of selectedDestinations) {
          try {
            coordinates[dest] = await geocodeLocation(dest);
          } catch (error) {
            console.error(`Failed to geocode ${dest}:`, error);
            Swal.fire({
              title: "Location Error",
              text: `Could not find coordinates for ${dest}. Please check the location name and try again.`,
              icon: "error",
              confirmButtonText: "OK",
            });
            return;
          }
        }

        selectedDestinations.forEach((dest1) => {
          newDistanceMatrix[dest1] = {};
          selectedDestinations.forEach((dest2) => {
            if (dest1 !== dest2) {
              const coord1 = coordinates[dest1];
              const coord2 = coordinates[dest2];
              newDistanceMatrix[dest1][dest2] = calculateDistance(
                coord1.lat,
                coord1.lng,
                coord2.lat,
                coord2.lng
              );
            }
          });
        });

        setDistanceMatrix(newDistanceMatrix);
      } catch (error) {
        console.error("Error calculating distances:", error);
        Swal.fire({
          title: "Error",
          text: "Failed to calculate distances between destinations. Please try again.",
          icon: "error",
          confirmButtonText: "OK",
        });
      }
    };

    calculateDistances();
  }, [selectedDestinations]);

  useEffect(() => {
    if (selectedDestinations.length < 2) {
      setIsValidRoute(true);
      return;
    }

    setIsValidRoute(true);
  }, [selectedDestinations, destinationData, distanceMatrix]);

  const getClimatePrediction = async (destination: string, month: string) => {
    try {
      console.log(destination, month);
      const response = await axios.post(
        "https://climate-443968559259.us-central1.run.app",
        {
          destination: destination,
          month: month,
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      console.log(response);
      return response.data.predicted_climate;
    } catch (error) {
      console.error("Error getting climate prediction:", error);
      return "Unknown";
    }
  };

  const handleDestinationChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
    dest: string
  ) => {
    if (e.target.checked) {
      setSelectedDestinations([...selectedDestinations, dest]);
      const climate = await getClimatePrediction(dest, formData.month);
      setDestinationData((prev) => ({
        ...prev,
        [dest]: {
          name: dest,
          days: "",
          activities: "",
          specialRequirements: "",
          climate,
        },
      }));
    } else {
      setSelectedDestinations(selectedDestinations.filter((d) => d !== dest));
      setDestinationData((prev) => {
        const newData = { ...prev };
        delete newData[dest];
        return newData;
      });
    }
  };

  const handleDestinationDataChange = (
    dest: string,
    field: keyof DestinationData,
    value: string
  ) => {
    setDestinationData((prev) => ({
      ...prev,
      [dest]: {
        ...prev[dest],
        [field]: value,
      },
    }));
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    setShowOptions(false);
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const missingDays = selectedDestinations.filter(
      (dest) => !destinationData[dest]?.days
    );

    if (missingDays.length > 0) {
      Swal.fire({
        title: "Missing Information",
        text: `Please select the number of days for: ${missingDays.join(", ")}`,
        icon: "warning",
        confirmButtonText: "OK",
      });
      return;
    }

    if (!formData.month) {
      Swal.fire({
        title: "Missing Information",
        text: "Please select a month for your trip",
        icon: "warning",
        confirmButtonText: "OK",
      });
      return;
    }

    try {
      const coordinates: { [key: string]: { lat: number; lng: number } } = {};
      const newDistanceMatrix: DistanceMatrix = {};

      for (const dest of selectedDestinations) {
        try {
          coordinates[dest] = await geocodeLocation(dest);
        } catch (error) {
          console.error(`Failed to geocode ${dest}:`, error);
          Swal.fire({
            title: "Location Error",
            text: `Could not find coordinates for ${dest}. Please check the location name and try again.`,
            icon: "error",
            confirmButtonText: "OK",
          });
          return;
        }
      }

      selectedDestinations.forEach((dest1) => {
        newDistanceMatrix[dest1] = {};
        selectedDestinations.forEach((dest2) => {
          if (dest1 !== dest2) {
            const coord1 = coordinates[dest1];
            const coord2 = coordinates[dest2];
            newDistanceMatrix[dest1][dest2] = calculateDistance(
              coord1.lat,
              coord1.lng,
              coord2.lat,
              coord2.lng
            );
          }
        });
      });

      setDistanceMatrix(newDistanceMatrix);

      const totalDays = Object.values(destinationData).reduce(
        (sum, data) => sum + (parseInt(data.days) || 0),
        0
      );
      const maxPossibleDistance = totalDays * MAX_DAILY_DISTANCE;

      let totalDistance = 0;
      for (let i = 0; i < selectedDestinations.length - 1; i++) {
        const current = selectedDestinations[i];
        const next = selectedDestinations[i + 1];
        const distance = newDistanceMatrix[current]?.[next] || 0;
        totalDistance += distance;
      }

      if (totalDistance > maxPossibleDistance) {
        Swal.fire({
          title: "Route Not Possible",
          html: `The selected destinations cannot be reached within the chosen days.<br>
                 Total distance: ${Math.round(totalDistance)}km<br>
                 Maximum possible distance: ${maxPossibleDistance}km<br>
                 Please adjust your destinations or increase the number of days.`,
          icon: "warning",
          confirmButtonText: "OK",
        });
        return;
      }

      Swal.fire({
        title: "Processing...",
        html: 'Please wait while we calculate distances and process your trip destinations.<br><div class="spinner-border" role="status"></div>',
        allowOutsideClick: false,
        showCancelButton: false,
        showConfirmButton: false,
      });

      const allResponses = [];

      for (const dest of selectedDestinations) {
        const climate = await getClimatePrediction(dest, formData.month);

        const destinationInfo = {
          Destination: dest,
          Month: formData.month,
          "Travel Duration":
            destinationData[dest].days +
            " Day" +
            (parseInt(destinationData[dest].days) > 1 ? "s" : ""),
          Climate: climate,
          Activites: destinationData[dest].activities,
          "Special Requirements": destinationData[dest].specialRequirements,
        };

        console.log("Processing destination:", destinationInfo);

        try {
          const response = await axios.post(
            "https://smarttripplannerupdated-443968559259.us-central1.run.app",
            destinationInfo,
            {
              headers: {
                "Content-Type": "application/json",
              },
            }
          );
          console.log("Destination response:", response.data);
          allResponses.push(response.data);
        } catch (apiError) {
          console.error("API Error, falling back to Gemini AI:", apiError);

          const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

          const prompt = `Generate a detailed ${destinationInfo["Travel Duration"]} trip plan for ${destinationInfo.Destination} in ${destinationInfo.Month} with ${destinationInfo.Climate} weather. 
          Activities to include: ${destinationInfo.Activites}
          Special requirements: ${destinationInfo["Special Requirements"]}
          write a plan for the above details as an example in below format
          
          "Day 01:
          Morning
          6:00 AM - Departure to Trincomalee
          Begin your journey early to maximize the day. Enjoy the scenic coastal route if traveling by private vehicle or train.
          8:00 AM - Breakfast at a Local Café
          Try a traditional Sri Lankan breakfast: string hoppers with curry or kiribath (milk rice).
          9:30 AM - Visit Koneswaram Temple
          Explore the iconic ""Temple of a Thousand Pillars,"" offering breathtaking views of the Indian Ocean.
          Recommended Time: 1.5 hours.
          11:00 AM - Explore Fort Frederick
          Stroll through this historical Portuguese-built fort. Great for photography and history enthusiasts.
          Afternoon
          1:00 PM - Seafood Lunch
          Head to a beachfront restaurant to enjoy specialties like crab curry, grilled fish, or prawns.
          2:30 PM - Relax at Marble Beach
          Spend the afternoon soaking up the sun and relaxing by the serene waters of Marble Beach.
          Recommended Time: 2.5 hours.
          5:00 PM - Swami Rock Cliff
          Capture dramatic views and enjoy the peaceful ambiance of this scenic spot.
          Evening
          6:30 PM - Sunset at Dutch Bay
          Watch the beautiful sunset and take photos as the sky turns golden.
          8:00 PM - Dinner and Nightlife
          Visit a beachfront restaurant for fresh seafood and cocktails.
          Head to a nearby bar or beach party for an exciting nightlife experience.
          Day 02 :
          Morning
          7:30 AM - Sunrise at Nilaveli Beach
          Start your day with a peaceful sunrise at Nilaveli Beach.
          8:30 AM - Breakfast at Hotel/Resort
          Enjoy a hearty breakfast at your accommodation or a nearby café.
          10:00 AM - Snorkeling and Water Sports
          Visit Pigeon Island for snorkeling or engage in other water sports like jet skiing or kayaking.
          Recommended Time: 2 hours.
          Afternoon
          1:00 PM - Lunch at a Local Restaurant
          Try different Sri Lankan and international dishes.
          3:00 PM - High Tea Buffet
          Indulge in a luxurious high tea buffet at a resort. Savor pastries, sandwiches, and Ceylon tea.
          4:30 PM - Relax at Uppuveli Beach
          Spend a relaxing afternoon at Uppuveli Beach, a quieter alternative to Marble Beach.
          Evening
          6:00 PM - Sunset and Evening Stroll
          Walk along the beach and enjoy the cool sea breeze.
          7:30 PM - Nightlife and Dinner
          Explore beach bars for live music and cocktails, followed by dinner with fresh seafood or Sri Lankan dishes.
          Day 03 :
          Morning
          7:30 AM - Yoga or Morning Walk on the Beach
          Begin the day with a refreshing yoga session or a walk along the beach.
          8:30 AM - Breakfast at Hotel/Resort
          Relish a traditional Sri Lankan or continental breakfast.
          10:00 AM - Whale Watching Tour
          Embark on a whale-watching tour (January is ideal for spotting blue whales).
          Recommended Time: 2-3 hours.
          Afternoon
          1:00 PM - Lunch at a Beachfront Restaurant
          Enjoy a relaxed meal featuring local flavors and fresh seafood.
          3:00 PM - Relax at a Beachfront Resort
          Spend your final afternoon unwinding at a resort or indulging in a spa treatment.
          Evening
          5:30 PM - Farewell Sunset at Nilaveli Beach
          Take one last walk along the beach and enjoy a memorable sunset.
          7:00 PM - Dinner and Departure
          Enjoy your final dinner in Trincomalee at a fine-dining restaurant or beachfront café before heading back.
          "`;

          try {
            const result = await model.generateContent(prompt);
            const response = await result.response;
            const tripPlan = response.text();
            allResponses.push({ prediction: tripPlan });
          } catch (geminiError) {
            console.error(
              "Error generating trip plan with Gemini:",
              geminiError
            );
            const basicPlan = `Day 1:
            Morning
            9:00 AM - Arrival and check-in
            10:00 AM - Start exploring ${destinationInfo.Destination}

            Afternoon
            1:00 PM - Lunch at a local restaurant
            2:30 PM - Continue exploring attractions

            Evening
            6:00 PM - Dinner
            7:30 PM - Evening activities`;

            allResponses.push({ prediction: basicPlan });
          }
        }
      }

      const combinedResponse = {
        destinations: allResponses,
        destinationsOrder: selectedDestinations.reduce((acc, dest, index) => {
          acc[`Destination ${index + 1}`] = dest;
          return acc;
        }, {} as { [key: string]: string }),
        totalDuration:
          Object.values(destinationData).reduce(
            (sum, data) => sum + parseInt(data.days),
            0
          ) + " Days",
        month: formData.month,
        totalDistance: Math.round(totalDistance),
        maxPossibleDistance: maxPossibleDistance,
      };

      console.log("Combined response:", combinedResponse);

      const id = localStorage.getItem("id");
      if (!id) {
        console.error("User ID not found in localStorage");
        Swal.fire("Error", "User ID not found", "error");
        return;
      }

      const dataToStore = {
        ...combinedResponse,
        timestamp: new Date().toISOString(),
      };

      const tripRef = ref(database, `travelAreas/${id}`);
      await push(tripRef, dataToStore);

      Swal.fire({
        title: "Success!",
        text: "Smart Trip Created Successfully!",
        icon: "success",
        confirmButtonText: "OK",
      }).then(() => {
        router.push({
          pathname: "/smartTripPlanner/UploadedResult",
          query: { data: JSON.stringify(combinedResponse) },
        });
      });
    } catch (error) {
      console.error("Error submitting data:", error);
      Swal.fire(
        "Error",
        "Failed to create smart trip. Please try again.",
        "error"
      );
    }
  };

  return (
    <main className="flex flex-col h-screen w-screen">
      <div className="w-full bg-white shadow-md z-10">
        <NIV />
      </div>
      <div className="flex flex-col lg:flex-row h-full relative">
        <div className="w-full lg:w-1/2 h-screen">
          <Image
            className="object-cover w-full h-full"
            src={image3}
            alt="Login Image"
            priority
          />
        </div>
        <div className="w-full lg:w-1/2 ml-auto flex flex-col items-center bg-gradient-to-b from-blue-200 to-blue-400 overflow-y-auto p-6 h-screen">
          <div className="relative w-full text-center mb-8">
            <h1 className="text-2xl lg:text-4xl font-semibold text-black mt-14">
              SUGGEST DESTINATION PLAN
            </h1>
            <button
              onClick={() => router.push("/smartTripPlanner/AllResults")}
              className="absolute top-1 right-2 flex items-center gap-2 py-2 px-3 bg-indigo-600 text-white rounded-t-lg hover:bg-indigo-700"
            >
              <Image src={image0} alt="Icon" width={20} height={20} />
              Planners
            </button>
          </div>
          <h5 className="text-center text-zinc-600 font-semibold text-sm leading-tight font-poppins mb-8">
            Fill out the following form to get your recommended plans in Sri
            Lanka.
          </h5>
          <form onSubmit={handleSubmit} className="space-y-4 w-full max-w-lg">
            <div className="flex flex-col space-y-4">
              <div className="relative w-full">
                <label className="text-gray-600 block mb-2">Destinations</label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-h-64 overflow-y-auto p-2 border rounded-md">
                  {destinations.map((dest) => (
                    <div key={dest} className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        id={`dest-${dest}`}
                        checked={selectedDestinations.includes(dest)}
                        onChange={(e) => handleDestinationChange(e, dest)}
                        className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                      />
                      <label htmlFor={`dest-${dest}`} className="text-gray-700">
                        {dest}
                      </label>
                    </div>
                  ))}
                </div>
              </div>

              {selectedDestinations.map((dest) => (
                <div
                  key={dest}
                  className="bg-white p-4 rounded-md shadow-sm space-y-4"
                >
                  <h3 className="text-lg font-semibold text-gray-800">
                    {dest}
                  </h3>

                  <div className="flex items-center space-x-4">
                    <span className="text-gray-600 w-1/3">Days</span>
                    <select
                      value={destinationData[dest]?.days}
                      onChange={(e) =>
                        handleDestinationDataChange(
                          dest,
                          "days",
                          e.target.value
                        )
                      }
                      className="w-2/3 px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                    >
                      <option>Select Days</option>
                      <option value="1">1 Day</option>
                      <option value="2">2 Days</option>
                      <option value="3">3 Days</option>
                      <option value="4">4 Days</option>
                      <option value="5">5 Days</option>
                    </select>
                  </div>

                  <div className="space-y-2">
                    <label className="text-gray-600">Activities</label>
                    <input
                      type="text"
                      value={destinationData[dest]?.activities || ""}
                      onChange={(e) =>
                        handleDestinationDataChange(
                          dest,
                          "activities",
                          e.target.value
                        )
                      }
                      placeholder="Enter activities for this destination"
                      className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-gray-600">
                      Special Requirements
                    </label>
                    <input
                      type="text"
                      value={destinationData[dest]?.specialRequirements || ""}
                      onChange={(e) =>
                        handleDestinationDataChange(
                          dest,
                          "specialRequirements",
                          e.target.value
                        )
                      }
                      placeholder="Enter special requirements for this destination"
                      className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                    />
                  </div>
                </div>
              ))}
            </div>

            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full">
                <label htmlFor="month" className="text-gray-600">
                  Month
                </label>
                <select
                  id="month"
                  name="month"
                  required
                  value={formData.month}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Month
                  </option>
                  <option value="January">January</option>
                  <option value="February">February</option>
                  <option value="March">March</option>
                  <option value="April">April</option>
                  <option value="May">May</option>
                  <option value="June">June</option>
                  <option value="July">July</option>
                  <option value="August">August</option>
                  <option value="September">September</option>
                  <option value="October">October</option>
                  <option value="November">November</option>
                  <option value="December">December</option>
                </select>
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-2 px-4 rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              Submit
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}
