const Footer = () => {
  return (
    <footer className="bg-gray-800 text-white py-4">
      <div className="container mx-auto text-center">
        <p>
          &copy; {new Date().getFullYear()} Ceylon Travelers. All rights
          reserved.
        </p>
        <div className="mt-2 flex justify-center space-x-4">
          <a
            href="#"
            className="text-gray-400 hover:text-white transition duration-300"
            aria-label="Facebook"
          >
            Facebook
          </a>
          <a
            href="#"
            className="text-gray-400 hover:text-white transition duration-300"
            aria-label="Twitter"
          >
            Twitter
          </a>
          <a
            href="#"
            className="text-gray-400 hover:text-white transition duration-300"
            aria-label="Instagram"
          >
            Instagram
          </a>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
