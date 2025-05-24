import React, { useState } from "react";
import NIV from "@/components/niv";
import image5 from "../../../../public/assets/pngtree-travel-around-the-world-background-picture-image_2253108.jpg";
import Image from "next/image";
import { useRouter } from "next/router";
import axios from "axios";
import image3 from "../../../../public/assets/12a4402a5b0390e771dc31c33e91b1d7.jpeg";
import Swal from "sweetalert2";

export default function Feedback() {
  const [formData, setFormData] = useState({
    favorite_activities: "",
    budget: "",
    num_persons: "",
    transportation: "",
    month: "",
    location: "",
    health_condition: "",
    accommodation_preferences: "",
    safty_concerns: "",
    dieatary_needs: "",
    pet_friendly_option: "",
    connectivity: "",
    language_preference: "",
    shoppping_preferences: "",
    accessibility_needs: "",
    Agency: "",
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
        "Accomadation Preferences": formData.accommodation_preferences,
        "Safty Concersn": formData.safty_concerns,
        "Dieatary Needs ": formData.dieatary_needs,
        "Pet Friendly Option": formData.pet_friendly_option,
        Connectivity: formData.connectivity,
        "Language Preference ": formData.language_preference,
        "Shoppping Preferences": formData.shoppping_preferences,
        "Accessibility needs": formData.accessibility_needs,
        Agency: formData.Agency,
      },
    };

    console.log("transformedData", transformedData);

    Swal.fire({
      title: "Processing...",
      text: "Please wait while we submit your data.",
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => {
        Swal.showLoading();
      },
    });

    try {
      const response = await axios.post(
        "https://agency2-443968559259.us-central1.run.app",
        transformedData
      );
      console.log("Response Data:", response.data);
      if (response.data === "Data saved and Excel updated successfully.") {
        Swal.fire({
          icon: "success",
          title: "Submitted Successfully!",
          text: "Data saved and Excel updated successfully.",
          confirmButtonText: "OK",
        }).then(() => {
          window.location.reload();
        });
      }
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

        <div className="w-full lg:w-1/2 ml-auto flex flex-col items-center bg-white overflow-y-auto p-6 h-screen">
          <div className="relative w-full text-center mb-8">
            <h1 className="text-2xl lg:text-4xl font-semibold text-black mt-14">
              Give Us Your Feedback
            </h1>
          </div>
          <h5 className="text-center text-zinc-600 font-semibold text-sm leading-tight font-poppins mb-8">
            Please provide us with the following information to help us
            customize your travel experience
          </h5>
          <form onSubmit={handleSubmit} className="space-y-4 w-full max-w-lg">
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="favorite_activities" className="text-gray-600">
                  Favourite Activity
                </label>
                <input
                  type="text"
                  id="favorite_activities"
                  name="favorite_activities"
                  required
                  value={formData.favorite_activities}
                  onChange={handleChange}
                  placeholder="Enter your favorite activity"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="budget" className="text-gray-600">
                  Budget
                </label>
                <input
                  type="number"
                  id="budget"
                  name="budget"
                  required
                  value={formData.budget}
                  onChange={handleChange}
                  placeholder="Enter your budget in USD"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="num_persons" className="text-gray-600">
                  Number of Persons
                </label>
                <input
                  type="number"
                  id="num_persons"
                  name="num_persons"
                  required
                  min="1"
                  max="10"
                  value={formData.num_persons}
                  onChange={handleChange}
                  placeholder="Enter number of persons"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="transportation" className="text-gray-600">
                  Transportation
                </label>
                <input
                  type="text"
                  id="transportation"
                  name="transportation"
                  required
                  value={formData.transportation}
                  onChange={handleChange}
                  placeholder="Enter your transportation preference"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="month" className="text-gray-600">
                  Month
                </label>
                <input
                  type="text"
                  id="month"
                  name="month"
                  required
                  value={formData.month}
                  onChange={handleChange}
                  placeholder="Enter month"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="location" className="text-gray-600">
                  Location
                </label>
                <input
                  type="text"
                  id="location"
                  name="location"
                  required
                  value={formData.location}
                  onChange={handleChange}
                  placeholder="Enter your preferred location"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="health_condition" className="text-gray-600">
                  Health Condition
                </label>
                <input
                  type="text"
                  id="health_condition"
                  name="health_condition"
                  required
                  value={formData.health_condition}
                  onChange={handleChange}
                  placeholder="Enter your health condition"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="accommodation_preferences"
                  className="text-gray-600"
                >
                  Accommodation Preferences
                </label>
                <input
                  type="text"
                  id="accommodation_preferences"
                  name="accommodation_preferences"
                  required
                  value={formData.accommodation_preferences}
                  onChange={handleChange}
                  placeholder="Enter your accommodation preference"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="safty_concerns" className="text-gray-600">
                  Safety Concerns
                </label>
                <input
                  type="text"
                  id="safty_concerns"
                  name="safty_concerns"
                  required
                  value={formData.safty_concerns}
                  onChange={handleChange}
                  placeholder="Enter your safety concern"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="dieatary_needs" className="text-gray-600">
                  Dietary Needs
                </label>
                <input
                  type="text"
                  id="dieatary_needs"
                  name="dieatary_needs"
                  required
                  value={formData.dieatary_needs}
                  onChange={handleChange}
                  placeholder="Enter your dietary needs"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="pet_friendly_option" className="text-gray-600">
                  Pet Friendly Option
                </label>
                <input
                  type="text"
                  id="pet_friendly_option"
                  name="pet_friendly_option"
                  required
                  value={formData.pet_friendly_option}
                  onChange={handleChange}
                  placeholder="Enter pet friendly option"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label htmlFor="connectivity" className="text-gray-600">
                  Connectivity
                </label>
                <input
                  type="text"
                  id="connectivity"
                  name="connectivity"
                  required
                  value={formData.connectivity}
                  onChange={handleChange}
                  placeholder="Enter connectivity option"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="language_preference" className="text-gray-600">
                  Language Preference
                </label>
                <input
                  type="text"
                  id="language_preference"
                  name="language_preference"
                  required
                  value={formData.language_preference}
                  onChange={handleChange}
                  placeholder="Enter preferred language"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>

              <div className="relative w-full lg:w-1/2">
                <label
                  htmlFor="shoppping_preferences"
                  className="text-gray-600"
                >
                  Shopping Preferences
                </label>
                <input
                  type="text"
                  id="shoppping_preferences"
                  name="shoppping_preferences"
                  required
                  value={formData.shoppping_preferences}
                  onChange={handleChange}
                  placeholder="Enter shopping preference (Low or High)"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
            </div>
            <div className="flex flex-col lg:flex-row space-y-4 lg:space-y-0 lg:space-x-4">
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="accessibility_needs" className="text-gray-600">
                  Accessibility Needs
                </label>
                <input
                  type="text"
                  id="accessibility_needs"
                  name="accessibility_needs"
                  required
                  value={formData.accessibility_needs}
                  onChange={handleChange}
                  placeholder="Enter accessibility needs (e.g., Wheel Chair)"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
              </div>
              <div className="relative w-full lg:w-1/2">
                <label htmlFor="Agency" className="text-gray-600">
                  Agency
                </label>
                <input
                  type="text"
                  id="Agency"
                  name="Agency"
                  required
                  value={formData.Agency}
                  onChange={handleChange}
                  placeholder="Enter Agency"
                  className="w-full px-4 py-2 rounded-md shadow-sm text-gray-900 ring-1 ring-gray-300 focus:ring-indigo-600"
                />
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
