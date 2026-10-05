/**
 * Employee Management System - Face Scanner Attendance
 * Powered by face-api.js with local model loading & CDN fallback
 */

let modelsLoaded = false;
let webcamStream = null;
let detectionInterval = null;

// Initialize models when page is loaded
window.addEventListener('DOMContentLoaded', async () => {
  await loadFaceModels();
});

async function loadFaceModels() {
  updateScannerStatus('Loading face detection AI models...', '#2563eb');
  try {
    // Attempt local load first
    await Promise.all([
      faceapi.nets.tinyFaceDetector.loadFromUri('/models'),
      faceapi.nets.faceLandmark68Net.loadFromUri('/models'),
      faceapi.nets.faceRecognitionNet.loadFromUri('/models')
    ]);
    modelsLoaded = true;
    updateScannerStatus('Face recognition models ready. Click "Start Camera" to begin.', '#059669');
  } catch (err) {
    console.warn('Local models failed to load, trying online weights fallback...', err);
    try {
      const cdnPath = 'https://raw.githubusercontent.com/justadudewhohacks/face-api.js/master/weights';
      await Promise.all([
        faceapi.nets.tinyFaceDetector.loadFromUri(cdnPath),
        faceapi.nets.faceLandmark68Net.loadFromUri(cdnPath),
        faceapi.nets.faceRecognitionNet.loadFromUri(cdnPath)
      ]);
      modelsLoaded = true;
      updateScannerStatus('Face recognition models loaded via CDN. Click "Start Camera".', '#059669');
    } catch (fallbackErr) {
      console.error('All model loading attempts failed:', fallbackErr);
      updateScannerStatus('Could not load face detection models. Please check network/browser settings.', '#dc2626');
    }
  }
}

// Start Webcam
async function startWebcam() {
  if (!modelsLoaded) {
    showToast('Face recognition models are still loading. Please wait a moment.', 'warning');
    return;
  }

  const video = document.getElementById('webcamVideo');
  const placeholder = document.getElementById('cameraPlaceholder');

  updateScannerStatus('Requesting webcam access...', '#2563eb');

  try {
    webcamStream = await navigator.mediaDevices.getUserMedia({
      video: { width: 640, height: 480, facingMode: 'user' }
    });

    video.srcObject = webcamStream;
    video.style.display = 'block';
    if (placeholder) placeholder.style.display = 'none';

    video.onloadedmetadata = () => {
      video.play();
      document.getElementById('startCameraBtn').style.display = 'none';
      document.getElementById('stopCameraBtn').style.display = 'inline-flex';
      document.getElementById('enrollFaceBtn').disabled = false;
      document.getElementById('facePunchBtn').disabled = false;
      updateScannerStatus('Camera active. Center your face inside the box.', '#059669');
      startLiveDetectionLoop();
    };
  } catch (err) {
    console.error('Webcam error:', err);
    updateScannerStatus('Webcam access denied or unavailable: ' + err.message, '#dc2626');
    showToast('Unable to access webcam. Please allow camera permissions in your browser.', 'error');
  }
}

// Stop Webcam
function stopWebcam() {
  if (detectionInterval) {
    clearInterval(detectionInterval);
    detectionInterval = null;
  }

  if (webcamStream) {
    webcamStream.getTracks().forEach(track => track.stop());
    webcamStream = null;
  }

  const video = document.getElementById('webcamVideo');
  const placeholder = document.getElementById('cameraPlaceholder');
  const canvas = document.getElementById('overlayCanvas');

  video.style.display = 'none';
  if (placeholder) placeholder.style.display = 'block';
  if (canvas) {
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
  }

  document.getElementById('startCameraBtn').style.display = 'inline-flex';
  document.getElementById('stopCameraBtn').style.display = 'none';
  document.getElementById('enrollFaceBtn').disabled = true;
  document.getElementById('facePunchBtn').disabled = true;
  updateScannerStatus('Camera stopped.', '#64748b');
}

// Continuous Face Landmark / Box Overlay
function startLiveDetectionLoop() {
  const video = document.getElementById('webcamVideo');
  const canvas = document.getElementById('overlayCanvas');

  detectionInterval = setInterval(async () => {
    if (!video || video.paused || video.ended || !webcamStream) return;

    const displaySize = { width: video.videoWidth || 640, height: video.videoHeight || 480 };
    faceapi.matchDimensions(canvas, displaySize);

    try {
      const detection = await faceapi
        .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions({ inputSize: 320, scoreThreshold: 0.5 }))
        .withFaceLandmarks();

      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      if (detection) {
        const resized = faceapi.resizeResults(detection, displaySize);
        faceapi.draw.drawDetections(canvas, resized);
      }
    } catch (e) {
      // Ignore background loop exceptions
    }
  }, 300);
}

// Capture Face Descriptor
async function captureFaceDescriptor() {
  const video = document.getElementById('webcamVideo');
  if (!webcamStream || !video || video.paused) {
    updateScannerStatus('Please start the camera first.', '#dc2626');
    return null;
  }

  updateScannerStatus('Detecting face & analyzing landmarks...', '#2563eb');

  try {
    const detection = await faceapi
      .detectSingleFace(video, new faceapi.TinyFaceDetectorOptions({ inputSize: 416, scoreThreshold: 0.5 }))
      .withFaceLandmarks()
      .withFaceDescriptor();

    if (!detection) {
      updateScannerStatus('Please keep your face inside the camera.', '#dc2626');
      showToast('Please keep your face inside the camera.', 'warning');
      return null;
    }

    return Array.from(detection.descriptor);
  } catch (err) {
    console.error('Face capture error:', err);
    updateScannerStatus('Detection failed: ' + err.message, '#dc2626');
    return null;
  }
}

// Enroll Face Action
async function enrollCurrentFace() {
  const enrollBtn = document.getElementById('enrollFaceBtn');
  enrollBtn.disabled = true;
  enrollBtn.textContent = 'Enrolling...';

  try {
    const descriptor = await captureFaceDescriptor();
    if (!descriptor) return;

    updateScannerStatus('Saving biometric face descriptor in MySQL...', '#2563eb');

    const res = await fetch('/api/employee/face/enroll', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ descriptor })
    });

    const data = await res.json();
    if (res.ok && data.success) {
      updateScannerStatus('Face enrolled successfully.', '#059669');
      showToast('Face enrolled successfully.', 'success');
      loadTodayStatus();
    } else {
      updateScannerStatus(data.message || 'Face enrollment failed.', '#dc2626');
      showToast(data.message || 'Face enrollment failed.', 'error');
    }
  } catch (err) {
    updateScannerStatus('Network error during face enrollment.', '#dc2626');
    showToast('Network error during face enrollment.', 'error');
  } finally {
    enrollBtn.disabled = false;
    enrollBtn.textContent = 'Enroll Face';
  }
}

// Face Punch Action (Punch In / Punch Out)
async function punchWithCurrentFace() {
  const punchBtn = document.getElementById('facePunchBtn');
  punchBtn.disabled = true;
  punchBtn.textContent = 'Verifying Face...';

  try {
    const descriptor = await captureFaceDescriptor();
    if (!descriptor) return;

    updateScannerStatus('Comparing face with enrolled biometric record...', '#2563eb');

    const res = await fetch('/api/employee/face/punch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ descriptor })
    });

    const data = await res.json();
    if (res.ok && data.success) {
      if (data.action === 'PUNCH_IN') {
        updateScannerStatus('Punch In successful.', '#059669');
        showToast('Punch In successful.', 'success');
      } else if (data.action === 'PUNCH_OUT') {
        const msg = `Punch Out successful. Work hours: ${data.workHours || ''}`;
        updateScannerStatus(msg, '#059669');
        showToast('Punch Out successful.', 'success');
      } else {
        updateScannerStatus(data.message || 'Attendance logged.', '#059669');
        showToast(data.message || 'Attendance logged.', 'success');
      }
      loadTodayStatus();
      loadAttendanceHistory();
    } else {
      updateScannerStatus(data.message || 'Attendance recording failed.', '#dc2626');
      showToast(data.message || 'Attendance recording failed.', 'error');
    }
  } catch (err) {
    updateScannerStatus('Server error during face punch.', '#dc2626');
    showToast('Server error during face punch.', 'error');
  } finally {
    punchBtn.disabled = false;
    punchBtn.textContent = 'Face Punch';
  }
}

// Status message helper
function updateScannerStatus(message, color = '#334155') {
  const msgEl = document.getElementById('scannerMessage');
  if (msgEl) {
    msgEl.textContent = message;
    msgEl.style.color = color;
    msgEl.style.fontWeight = '600';
  }
}
