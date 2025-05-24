import Image from "next/image";
import { useState } from "react";
import axios from "axios";
import { ref, push, set, get } from "firebase/database";
import NIV from "@/components/niv";
import { app, database } from "../../../firebaseconfig";
import image3 from "../../../public/assets/7.jpg";
import Swal from "sweetalert2";
import { useRouter } from "next/router";
import image0 from "../../../public/assets/gedara.png";
import image1 from "../../../public/assets/feedback.png";

export default function Destination() {
  const [formData, setFormData] = useState({
    favorite_activities: "",
    budget: "",
    num_persons: "",
    transportation: "",
    month: "",
    location: "",
    health_condition: "",
    accomadation_preferences: "",
    safty_concerns: "",
    dieatary_needs: "",
    pet_friendly_option: "",
    connectivity: "",
    language_preference: "",
    shoppping_preferences: "",
    accessibility_needs: "",
  });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const transformedData = {
      features: {
        "Favorite Activities": formData.favorite_activities,
        Budget: formData.budget,
        "Number of Persons": formData.num_persons,
        "Transportation Options": formData.transportation,
        Month: formData.month,
        Place: formData.location,
        "Health Condition": formData.health_condition,
        "Accomadation Preferences": formData.accomadation_preferences,
        "Safty Concersn": formData.safty_concerns,
        "Dieatary Needs ": formData.dieatary_needs,
        "Pet Friendly Option": formData.pet_friendly_option,
        Connectivity: formData.connectivity,
        "Language Preference ": formData.language_preference,
        "Shoppping Preferences": formData.shoppping_preferences,
        "Accessibility needs": formData.accessibility_needs,
      },
    };

    console.log("transformedData", transformedData);

    Swal.fire({
      title: "Processing...",
      html: 'Please wait while the data is being processed.<br><div class="spinner-border" role="status"></div>',
      allowOutsideClick: false,
      showCancelButton: false,
      showConfirmButton: false,
    });

    try {
      const response = await axios.post(
        // "https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/agency",
        "https://agency-443968559259.us-central1.run.app",
        transformedData
      );
      console.log("Response Data:", response.data);

      const prediction = response.data?.prediction?.[0];
      if (!prediction || prediction.length === 0) {
        Swal.fire("Error", "No agency predictions found", "error");
        return;
      }

      const agency = prediction[0];
      console.log("Extracted Agency:", agency);

      // const dataWithAgency = {
      //   ...transformedData.features,
      //   Agency: agency,
      // };

      // console.log("Data with Agency:", dataWithAgency);

      // const agencyResponse = await axios.post(
      //   "https://us-central1-custom-repeater-446305-a4.cloudfunctions.net/agency2",
      //   { features: dataWithAgency }
      // );
      // console.log("Agency Response:", agencyResponse.data);

      const id = localStorage.getItem("id");
      if (!id) {
        console.error("User ID not found in localStorage");
        Swal.fire("Error", "User ID not found", "error");
        return;
      }
      console.log("User ID:", id);
      console.log("Response Data:", response.data);

      const dataToStore = {
        ...response.data,
        timestamp: new Date().toISOString(),
        transformedData: transformedData,
      };

      const tripRef = ref(database, `travel_agency/${id}`);
      await push(tripRef, dataToStore);

      const [travelAgencySnapshot, usersSnapshot] = await Promise.all([
        get(ref(database, "travel_agency")),
        get(ref(database, "user")),
      ]);

      const allTrips = travelAgencySnapshot.val();
      const allUsers = usersSnapshot.val();

      if (formData.num_persons === "1") {
        const matchingTrips = [];

        for (const userId in allTrips) {
          for (const tripId in allTrips[userId]) {
            const trip = allTrips[userId][tripId];
            if (
              trip.transformedData?.features?.Place === formData.location &&
              trip.transformedData?.features["Number of Persons"] ===
                formData.num_persons &&
              trip.transformedData?.features["Favorite Activities"] ===
                formData.favorite_activities &&
              trip.transformedData?.features.Month === formData.month &&
              userId !== id
            ) {
              const userData = allUsers[userId];
              matchingTrips.push({
                userId,
                tripId,
                uname: userData?.uname || "Anonymous User",
                email: userData?.email,
              });
            }
          }
        }

        if (matchingTrips.length > 0) {
          const { value: selectedTrips } = await Swal.fire({
            title: "Found Matching Travelers!",
            html: `
              <div class="text-left">
                <p>We found ${
                  matchingTrips.length
                } travelers with similar preferences:</p>
                <div class="max-h-60 overflow-y-auto mt-2">
                  ${matchingTrips
                    .map(
                      (trip) =>
                        `<div class="flex items-center mb-2">
                      <input type="checkbox" id="user_${trip.userId}_${trip.tripId}" 
                             class="mr-2" value="${trip.userId},${trip.tripId}">
                      <label for="user_${trip.userId}_${trip.tripId}">
                        ${trip.uname} (${trip.email})
                      </label>
                    </div>`
                    )
                    .join("")}
                </div>
              </div>
            `,
            showCancelButton: true,
            confirmButtonText: "Send Requests",
            cancelButtonText: "Skip",
            preConfirm: () => {
              const checkboxes = document.querySelectorAll(
                'input[type="checkbox"]:checked'
              ) as NodeListOf<HTMLInputElement>;
              return Array.from(checkboxes).map((cb) => cb.value);
            },
          });

          if (selectedTrips && selectedTrips.length > 0) {
            const requestRef = ref(database, "requests");

            for (const selectedTrip of selectedTrips) {
              const [selectedUserId, selectedTripId] = selectedTrip.split(",");

              const newRequestRef = push(requestRef);
              await set(newRequestRef, {
                fromUserId: id,
                fromUserName: allUsers[id]?.uname,
                toUserId: selectedUserId,
                toUserName: allUsers[selectedUserId]?.uname,
                tripId: selectedTripId,
                status: "pending",
                timestamp: new Date().toISOString(),
              });

              await axios.post("https://email-443968559259.us-central1.run.app", {
                email: [allUsers[selectedUserId]?.email],
                transformData: transformedData,
                receiver_name: allUsers[selectedUserId]?.uname,
                sender_name: allUsers[id]?.uname,
              });
            }

            await Swal.fire({
              title: "Requests Sent!",
              text: `Your travel buddy requests have been sent to ${selectedTrips.length} travelers!`,
              icon: "success",
              confirmButtonText: "OK",
            });
          }
        }
      }

      Swal.fire({
        title: "Success!",
        text: "Agency Created Successfully !",
        icon: "success",
        confirmButtonText: "OK",
      }).then(() => {
        router.push({
          pathname: "/agencies/UploadedResult",
          query: { data: JSON.stringify(response.data) },
        });
      });
    } catch (error) {
      console.error("Error submitting data:", error);
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
            alt="Travel Image"
            priority
          />
        </div>

        <div className="w-full lg:w-1/2 ml-auto flex flex-col items-center bg-gradient-to-b from-blue-200 to-blue-400 overflow-y-auto p-6 h-screen">
          <div className="relative w-full text-center mb-8">
            <h1 className="text-2xl lg:text-4xl font-semibold text-sky-700 mt-14">
              SUGGEST AGENCIES
            </h1>
            <button
              onClick={() => router.push("/agencies/AllResults")}
              className="absolute top-1 right-2 flex items-center gap-2 py-2 px-3 bg-indigo-600 text-white rounded-t-lg hover:bg-indigo-700"
            >
              <Image src={image0} alt="Icon" width={20} height={20} />
              Agencies
            </button>
            <button
              onClick={() => router.push("/agencies/Feedback")}
              className="absolute top-1 right-19 flex items-center gap-2 py-2 px-3 bg-green-600 text-white rounded-t-lg hover:bg-green-700"
            >
              <Image src={image1} alt="Icon" width={20} height={20} />
              Feedback
            </button>
          </div>
          <h5 className="text-center text-zinc-600 font-semibold text-sm leading-tight font-poppins mb-8">
            Fill out the following form to get your recommended plans in Sri
            Lanka.
          </h5>
          <form onSubmit={handleSubmit} className="space-y-4 w-full max-w-lg">
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Favourite Activity
                </label>
                <select
                  id="favorite_activities"
                  name="favorite_activities"
                  required
                  value={formData.favorite_activities}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select your favorite activity
                  </option>
                  <option value="Surfing">Surfing</option>
                  <option value="Hiking">Hiking</option>
                  <option value="Whale Watching">Whale Watching</option>
                  <option value="Cultural Tour">Cultural Tour</option>
                  <option value="Beach Relaxation">Beach Relaxation</option>
                  <option value="Wildlife Safari">Wildlife Safari</option>
                  <option value="Tea Plantation Tour">
                    Tea Plantation Tour
                  </option>
                  <option value="Scuba Diving">Scuba Diving</option>
                  <option value="Historical Site Visit">
                    Historical Site Visit
                  </option>
                  <option value="Lagoon Boat Ride">Lagoon Boat Ride</option>
                  <option value="Bird Watching">Bird Watching</option>
                  <option value="Deep-Sea Fishing">Deep-Sea Fishing</option>
                  <option value="City Walking Tour">City Walking Tour</option>
                  <option value="Kite Surfing">Kite Surfing</option>
                  <option value="Temple Visits">Temple Visits</option>
                  <option value="Cooking Classes">Cooking Classes</option>
                  <option value="Village Safari">Village Safari</option>
                  <option value="Ayurvedic Spa Retreat">
                    Ayurvedic Spa Retreat
                  </option>
                  <option value="Wildlife Photography">
                    Wildlife Photography
                  </option>
                  <option value="SNorkeling">Snorkeling</option>
                </select>
              </div>
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Travel_Dates_and_Duration"
                  className="text-gray-600"
                >
                  Budget
                </label>
                <select
                  id="budget"
                  name="budget"
                  required
                  value={formData.budget}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select your budget
                  </option>
                  <option value="$0 - $100">$0 - $100</option>
                  <option value="$100 - $200">$100 - $200</option>
                  <option value="$200 - $300">$200 - $300</option>
                  <option value="$300 - $400">$300 - $400</option>
                  <option value="$400+">$400+</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="Special_Requirements" className="text-gray-600">
                  Number of Persons
                </label>
                <select
                  id="num_persons"
                  name="num_persons"
                  required
                  value={formData.num_persons}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select number of persons
                  </option>
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                  <option value="4">4</option>
                  <option value="5">5</option>
                  <option value="6">6</option>
                  <option value="7">7</option>
                  <option value="8">8</option>
                  <option value="9">9</option>
                  <option value="10">10</option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="Travel_Style" className="text-gray-600">
                  Transportation
                </label>
                <select
                  id="transportation"
                  name="transportation"
                  required
                  value={formData.transportation}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select transportation
                  </option>
                  <option value="Private Vehicle">Private Vehicle</option>
                  <option value="Public Transport">Public Transport</option>
                  <option value="Bike and Bicycle">Bike and Bicycle</option>
                  <option value="Boat">Boat</option>
                  <option value="Train">Train</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
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
                    Select month
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

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="Climate" className="text-gray-600">
                  Location
                </label>
                <select
                  id="location"
                  name="location"
                  required
                  value={formData.location}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select a location
                  </option>
                  <option value="Yala National Park">Yala National Park</option>
                  <option value="Sinharaja Forest">Sinharaja Forest</option>
                  <option value="Habarana">Habarana</option>
                  <option value="Wilpattu National Park">
                    Wilpattu National Park
                  </option>
                  <option value="Hikkaduwa">Hikkaduwa</option>
                  <option value="Kandy">Kandy</option>
                  <option value="Polonnaruwa">Polonnaruwa</option>
                  <option value="Anuradhapura">Anuradhapura</option>
                  <option value="Nuwara Eliya">Nuwara Eliya</option>
                  <option value="Colombo">Colombo</option>
                  <option value="Mirissa">Mirissa</option>
                  <option value="Arugam Bay">Arugam Bay</option>
                  <option value="Unawatuna">Unawatuna</option>
                  <option value="Kalpitiya">Kalpitiya</option>
                  <option value="Bentota">Bentota</option>
                  <option value="Trincomalee">Trincomalee</option>
                  <option value="Pigeon Island">Pigeon Island</option>
                  <option value="Knuckles Range">Knuckles Range</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Health Condition
                </label>
                <select
                  id="health_condition"
                  name="health_condition"
                  required
                  value={formData.health_condition}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Health Condition
                  </option>
                  <option value="Pressure">Pressure</option>
                  <option value="Vomiting">Vomiting</option>
                  <option value="None">None</option>
                  <option value="Fainting">Fainting</option>
                  <option value="Heart Attack">Heart Attack</option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Travel_Dates_and_Duration"
                  className="text-gray-600"
                >
                  Accomadation Preferences
                </label>
                <select
                  id="accomadation_preferences"
                  name="accomadation_preferences"
                  required
                  value={formData.accomadation_preferences}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select a Accomadation Preferences
                  </option>
                  <option value="Hotel">Hotel</option>
                  <option value="House">House</option>
                  <option value="Villa">Villa</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Safty Concerns
                </label>
                <select
                  id="safty_concerns"
                  name="safty_concerns"
                  required
                  value={formData.safty_concerns}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select safty Concern
                  </option>
                  <option value="Heat">Heat</option>
                  <option value="Rain">Rain</option>
                  <option value="None">None</option>
                  <option value="Elephants Availability">
                    Elephants Availability
                  </option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Travel_Dates_and_Duration"
                  className="text-gray-600"
                >
                  Dieatary Needs
                </label>
                <select
                  id="dieatary_needs"
                  name="dieatary_needs"
                  required
                  value={formData.dieatary_needs}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Dieatary Needs
                  </option>
                  <option value="Western Menu">Western Menu</option>
                  <option value="Sri Lankan Menu">Sri Lankan Menu</option>
                  <option value="Indian Menu">Indian Menu</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Pet Friendly Option
                </label>
                <select
                  id="pet_friendly_option"
                  name="pet_friendly_option"
                  required
                  value={formData.pet_friendly_option}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Pet Friendly Option
                  </option>
                  <option value="Yes">Yes</option>
                  <option value="No">No</option>
                  <option value="Maybe">Maybe</option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Travel_Dates_and_Duration"
                  className="text-gray-600"
                >
                  Connectivity
                </label>
                <select
                  id="connectivity"
                  name="connectivity"
                  required
                  value={formData.connectivity}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select a Connectivity
                  </option>
                  <option value="Wifi">Wifi</option>
                  <option value="Mobile Data">Mobile Data</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Language Preference
                </label>
                <select
                  id="language_preference"
                  name="language_preference"
                  required
                  value={formData.language_preference}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Language
                  </option>
                  <option value="English">English</option>
                  <option value="Sinhala">Sinhala</option>
                  <option value="Tamil">Tamil</option>
                  <option value="Chinese">Chinese</option>
                  <option value="Spanish">Spanish</option>
                </select>
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Travel_Dates_and_Duration"
                  className="text-gray-600"
                >
                  Shoppping Preferences
                </label>
                <select
                  id="shoppping_preferences"
                  name="shoppping_preferences"
                  required
                  value={formData.shoppping_preferences}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Shoppping Preferences
                  </option>
                  <option value="Low">Low</option>
                  <option value="High">High</option>
                </select>
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="Destination_Preferences"
                  className="text-gray-600"
                >
                  Accessibility Needs
                </label>
                <select
                  id="accessibility_needs"
                  name="accessibility_needs"
                  required
                  value={formData.accessibility_needs}
                  onChange={handleChange}
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                >
                  <option value="" disabled>
                    Select Accessibility Needs
                  </option>
                  <option value="Wheel Chair">Wheel Chair</option>
                  <option value="None">None</option>
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
