import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [images, setImages] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [message, setMessage] = useState('');

  const apiEndpoint = 'http://localhost:8080'; // API is served from the same origin

  const fetchImages = async () => {
    try {
      const response = await axios.get(`${apiEndpoint}/api/images`);
      setImages(response.data);
    } catch (error) {
      console.error("Error fetching images:", error);
      setMessage('Could not fetch images.');
    }
  };

  useEffect(() => {
    fetchImages();
  }, []);

  const onFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
  };

  const onFileUpload = async () => {
    if (!selectedFile) {
      setMessage('Please select a file first.');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      setMessage('Uploading...');
      const response = await axios.post(`${apiEndpoint}/api/images/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      setMessage(response.data);
      setSelectedFile(null);
      document.getElementById('fileInput').value = null;
      fetchImages(); // Refresh the image list
    } catch (error) {
      console.error("Error uploading file:", error);
      setMessage('Error uploading file.');
    }
  };

  return (
      <div className="App">
        <header className="App-header">
          <h1>Image Gallery</h1>
          <p>Upload and view images stored in S3</p>
        </header>
        <div className="upload-section">
          <h2>Upload New Image</h2>
          <input id="fileInput" type="file" onChange={onFileChange} />
          <button onClick={onFileUpload}>Upload</button>
          {message && <p className="message">{message}</p>}
        </div>
        <div className="gallery-section">
          <h2>Gallery</h2>
          <div className="image-grid">
            {images.length > 0 ? (
                images.map((imgUrl, index) => (
                    <div key={index} className="image-card">
                      <img src={imgUrl} alt={`gallery item ${index}`} />
                    </div>
                ))
            ) : (
                <p>No images found.</p>
            )}
          </div>
        </div>
      </div>
  );
}

export default App;
