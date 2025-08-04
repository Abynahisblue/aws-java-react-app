import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [images, setImages] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [message, setMessage] = useState('');
  const [description, setDescription] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const apiEndpoint = 'http://webapp-prod-alb-1582454515.eu-central-1.elb.amazonaws.com' || 'http://localhost:8080'; // API is served from the same origin

  const fetchImages = async () => {
    try {
      const response = await axios.get(`${apiEndpoint}/api/images?page=${currentPage}&size=10`);
      
      // Handle paginated response
      if (response.data.content) {
        setImages(response.data.content);
        setTotalPages(response.data.totalPages);
      } else {
        // Handle simple array response
        setImages(response.data);
      }
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
    formData.append('description', description);

    try {
      setMessage('Uploading...');
      const response = await axios.post(`${apiEndpoint}/api/images/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setMessage(response.data);
      setSelectedFile(null);
      setDescription('');
      fetchImages();
    } catch (error) {
      setMessage('Error uploading file.');
    }
  };

  return (
      <div className="App">
        <header className="App-header">
          <h1>Image Gallery, (Upload your image)</h1>
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
                images.map((item, index) => (
                    <div key={index} className="image-card">
                      <img src={typeof item === 'string' ? item : item.s3Url} alt={`gallery item ${index}`} />
                      {typeof item === 'object' && item.description && (
                        <p className="image-description">{item.description}</p>
                      )}
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


