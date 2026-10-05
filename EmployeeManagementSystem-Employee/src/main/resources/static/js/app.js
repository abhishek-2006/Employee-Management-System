/**
 * JV Group Employee Management System (JV-GEMS)
 * Employee Portal JavaScript
 * Full implementation with Profile Management, Photo Uploads, and Attendance
 */

let currentEmployee = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
  const isAuth = await checkEmployeeAuth();
  if (!isAuth) return;

  // Set default dates for leave form
  const todayStr = new Date().toISOString().split('T')[0];
  const fromEl = document.getElementById('leaveFromDate');
  const toEl = document.getElementById('leaveToDate');
  if (fromEl) fromEl.value = todayStr;
  if (toEl) toEl.value = todayStr;

  // Initial load
  await loadTodayStatus();
  await loadProfile();
  loadAttendanceHistory();
  loadMyLeaves();
  loadNotifications();
});

// Check Employee Authentication
async function checkEmployeeAuth() {
  try {
    const res = await fetch('/api/employee/auth/status');
    const data = await res.json();
    if (!data.authenticated) {
      window.location.href = 'index.html?role=employee';
      return false;
    }
    currentEmployee = data.employee;
    updateEmployeeUI(currentEmployee);
    return true;
  } catch (err) {
    window.location.href = 'index.html?role=employee';
    return false;
  }
}

// Mobile sidebar toggle
function toggleMobileSidebar() {
  const sidebar = document.getElementById('appSidebar');
  if (sidebar) {
    sidebar.classList.toggle('mobile-open');
  }
}

// Update Employee UI elements across the page
function updateEmployeeUI(emp) {
  if (!emp) return;

  // Sidebar profile
  const nameEl = document.getElementById('empNameDisplay');
  const codeEl = document.getElementById('empCodeDisplay');
  const initialEl = document.getElementById('empAvatarInitial');
  const avatarImgEl = document.getElementById('empSidebarAvatarImg');

  if (nameEl) nameEl.textContent = emp.fullName;
  if (codeEl) codeEl.textContent = emp.employeeCode;

  if (emp.photo) {
    if (avatarImgEl) {
      avatarImgEl.src = emp.photo;
      avatarImgEl.style.display = 'block';
    }
    if (initialEl) initialEl.style.display = 'none';
  } else {
    if (avatarImgEl) avatarImgEl.style.display = 'none';
    if (initialEl) {
      initialEl.textContent = getInitials(emp.fullName);
      initialEl.style.display = 'inline-block';
    }
  }

  // Dashboard Welcome Banner
  const dName = document.getElementById('dashEmpFullName');
  const dCode = document.getElementById('dashEmpCode');
  const dDept = document.getElementById('dashEmpDept');
  const dDesig = document.getElementById('dashEmpDesig');

  if (dName) dName.textContent = emp.fullName;
  if (dCode) dCode.textContent = emp.employeeCode;
  if (dDept) dDept.textContent = emp.department || 'General';
  if (dDesig) dDesig.textContent = emp.designation || 'Staff';

  const bannerImg = document.getElementById('dashBannerImg');
  const bannerInitials = document.getElementById('dashBannerInitials');
  if (emp.photo) {
    if (bannerImg) {
      bannerImg.src = emp.photo;
      bannerImg.style.display = 'block';
    }
    if (bannerInitials) bannerInitials.style.display = 'none';
  } else {
    if (bannerImg) bannerImg.style.display = 'none';
    if (bannerInitials) {
      bannerInitials.textContent = getInitials(emp.fullName);
      bannerInitials.style.display = 'inline-block';
    }
  }

  // Dashboard Overview Card (Requirement 3)
  const oName = document.getElementById('dashOverviewName');
  const oCode = document.getElementById('dashOverviewCode');
  const oGender = document.getElementById('dashOverviewGender');
  const oDob = document.getElementById('dashOverviewDob');
  const oDept = document.getElementById('dashOverviewDept');
  const oDesig = document.getElementById('dashOverviewDesig');
  const oJoin = document.getElementById('dashOverviewJoinDate');
  const oStatus = document.getElementById('dashOverviewStatus');
  const oEmail = document.getElementById('dashOverviewEmail');
  const oPhone = document.getElementById('dashOverviewPhone');
  const oAddress = document.getElementById('dashOverviewAddress');

  if (oName) oName.textContent = emp.fullName;
  if (oCode) oCode.textContent = emp.employeeCode;
  if (oGender) oGender.textContent = emp.gender || 'Not Specified';
  if (oDob) oDob.textContent = emp.dob || 'Not provided';
  if (oDept) oDept.textContent = emp.department || 'General';
  if (oDesig) oDesig.textContent = emp.designation || 'Staff';
  if (oJoin) oJoin.textContent = emp.joinDate || 'Not specified';
  if (oStatus) {
    oStatus.innerHTML = emp.status === 'ACTIVE'
      ? '<span class="badge badge-active"><span class="status-dot"></span> Active</span>'
      : '<span class="badge badge-inactive">Inactive</span>';
  }
  if (oEmail) oEmail.textContent = emp.email;
  if (oPhone) oPhone.textContent = emp.phone || 'Not provided';
  if (oAddress) oAddress.textContent = emp.address || 'No residential address on file';
}

function getInitials(name) {
  if (!name) return 'JV';
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

// Logout
async function handleLogout() {
  if (!confirm('Are you sure you want to log out from JV-GEMS Employee Portal?')) return;
  try {
    if (typeof stopWebcam === 'function') stopWebcam();
    await fetch('/api/employee/auth/logout', { method: 'POST' });
  } finally {
    window.location.href = 'index.html?role=employee';
  }
}

// Switch Tab
function switchTab(tabName) {
  // If moving away from scanner, stop webcam
  if (tabName !== 'scanner' && typeof stopWebcam === 'function') {
    stopWebcam();
  }

  // Close mobile sidebar if open
  const sidebar = document.getElementById('appSidebar');
  if (sidebar) sidebar.classList.remove('mobile-open');

  const links = document.querySelectorAll('.menu-link');
  links.forEach(l => l.classList.remove('active'));

  const panes = document.querySelectorAll('.tab-pane');
  panes.forEach(p => p.classList.remove('active'));

  const targetPane = document.getElementById(`pane-${tabName}`);
  if (targetPane) targetPane.classList.add('active');

  const activeLink = Array.from(links).find(l => l.getAttribute('onclick')?.includes(tabName));
  if (activeLink) activeLink.classList.add('active');

  const titles = {
    'dashboard': 'Employee Dashboard',
    'scanner': 'Face Attendance Scanner',
    'history': 'Attendance History',
    'apply-leave': 'Apply for Leave',
    'my-leaves': 'My Leave Applications',
    'notifications': 'In-App Notifications',
    'profile': 'My Profile'
  };
  const titleEl = document.getElementById('currentSectionTitle');
  if (titleEl) titleEl.textContent = titles[tabName] || 'JV-GEMS Employee';

  // Refresh tab data
  if (tabName === 'dashboard') loadTodayStatus();
  if (tabName === 'history') loadAttendanceHistory();
  if (tabName === 'my-leaves') loadMyLeaves();
  if (tabName === 'notifications') loadNotifications();
  if (tabName === 'profile') loadProfile();
}

// Toast Alert
function showToast(message, type = 'info') {
  const toast = document.getElementById('toastAlert');
  if (!toast) return;
  toast.textContent = message;
  toast.className = `toast-alert show ${type}`;

  setTimeout(() => {
    toast.className = 'toast-alert';
  }, 4000);
}

// ================= SECTION 1: TODAY ATTENDANCE STATUS =================
let todayAttendanceStatus = null;

async function loadTodayStatus() {
  try {
    const res = await fetch('/api/employee/attendance/status');
    const data = await res.json();
    if (!data.success) return;

    todayAttendanceStatus = data;

    const statusTextEl = document.getElementById('todayStatusText');
    const punchInEl = document.getElementById('todayPunchInText');
    const punchOutEl = document.getElementById('todayPunchOutText');
    const workHoursEl = document.getElementById('todayWorkHoursText');

    if (punchInEl) punchInEl.textContent = data.punchInTime ? formatTime(data.punchInTime) : '-';
    if (punchOutEl) punchOutEl.textContent = data.punchOutTime ? formatTime(data.punchOutTime) : '-';
    if (workHoursEl) workHoursEl.textContent = data.formattedWorkHours || '0h 0m';

    if (statusTextEl) {
      if (!data.punchedIn) {
        statusTextEl.textContent = 'Not Punched Today';
        statusTextEl.style.color = 'var(--text-muted)';
      } else if (data.punchedIn && !data.punchedOut) {
        statusTextEl.textContent = 'Working (Punched In)';
        statusTextEl.style.color = '#059669';
      } else if (data.punchedOut) {
        statusTextEl.textContent = 'Day Completed (Punched Out)';
        statusTextEl.style.color = 'var(--primary)';
      }
    }

    // Update Action Buttons
    const btnIn = document.getElementById('btnPunchInAction');
    const btnOut = document.getElementById('btnPunchOutAction');

    if (btnIn) {
      if (data.punchedIn) {
        btnIn.disabled = true;
        btnIn.style.opacity = '0.5';
        btnIn.style.cursor = 'not-allowed';
        btnIn.title = 'Already punched in today';
      } else {
        btnIn.disabled = false;
        btnIn.style.opacity = '1';
        btnIn.style.cursor = 'pointer';
        btnIn.title = 'Click to Punch In with verification photo';
      }
    }

    if (btnOut) {
      if (!data.punchedIn) {
        btnOut.disabled = true;
        btnOut.style.opacity = '0.5';
        btnOut.style.cursor = 'not-allowed';
        btnOut.title = 'Please punch in before punching out';
      } else if (data.punchedOut) {
        btnOut.disabled = true;
        btnOut.style.opacity = '0.5';
        btnOut.style.cursor = 'not-allowed';
        btnOut.title = 'Already completed today\'s attendance';
      } else {
        btnOut.disabled = false;
        btnOut.style.opacity = '1';
        btnOut.style.cursor = 'pointer';
        btnOut.title = 'Click to Punch Out with verification photo';
      }
    }
  } catch (err) {
    console.error('Failed to load today status:', err);
  }
}

// ================= SECTION 2: MY PROFILE (Requirement 3 & 7) =================
async function loadProfile() {
  try {
    const res = await fetch('/api/employee/profile');
    const data = await res.json();
    if (!data.success || !data.profile) return;

    const p = data.profile;
    currentEmployee = p;
    updateEmployeeUI(p);

    // Profile page elements
    const nameEl = document.getElementById('profileFullName');
    const dNameEl = document.getElementById('profileDetailFullName');
    const desigEl = document.getElementById('profileDesignation');
    const dDesigEl = document.getElementById('profileDetailDesig');
    const codeEl = document.getElementById('profileEmpCode');
    const genderEl = document.getElementById('profileGender');
    const dobEl = document.getElementById('profileDob');
    const deptEl = document.getElementById('profileDepartment');
    const joinEl = document.getElementById('profileJoinDate');
    const emailEl = document.getElementById('profileEmail');
    const phoneEl = document.getElementById('profilePhone');
    const addressEl = document.getElementById('profileAddress');
    const faceEl = document.getElementById('profileFaceStatus');
    const largeImg = document.getElementById('profileLargeImg');
    const userEl = document.getElementById('profileUsernameVal');

    if (nameEl) nameEl.textContent = p.fullName;
    if (dNameEl) dNameEl.textContent = p.fullName;
    if (desigEl) desigEl.textContent = p.designation || 'Staff Member';
    if (dDesigEl) dDesigEl.textContent = p.designation || 'Staff Member';
    if (codeEl) codeEl.textContent = p.employeeCode;
    if (genderEl) genderEl.textContent = p.gender || 'Not Specified';
    if (dobEl) dobEl.textContent = p.dob || 'Not provided';
    if (deptEl) deptEl.textContent = p.department || 'General';
    if (joinEl) joinEl.textContent = p.joinDate || 'Not specified';
    if (emailEl) emailEl.textContent = p.email;
    if (phoneEl) phoneEl.textContent = p.phone || 'Not provided';
    if (addressEl) addressEl.textContent = p.address || 'No residential address on file';
    if (userEl) userEl.textContent = p.username || p.employeeCode;

    if (faceEl) {
      faceEl.innerHTML = p.faceEnrolled
        ? '<span class="badge badge-approved">Enrolled & Verified</span>'
        : '<span class="badge badge-pending">Pending Setup</span>';
    }

    if (largeImg && p.photo) {
      largeImg.src = p.photo;
    }
  } catch (err) {
    console.error('Failed to load employee profile:', err);
  }
}

// Upload Employee Profile Photo (Self-Service)
function handleSelfPhotoUpload(input) {
  if (!input.files || !input.files[0]) return;
  const file = input.files[0];

  if (!file.type.startsWith('image/')) {
    showToast('Please select a valid image file', 'error');
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    showToast('Image file size must be under 5MB', 'error');
    return;
  }

  const reader = new FileReader();
  reader.onload = async function(e) {
    const base64 = e.target.result;

    try {
      showToast('Uploading profile photo...', 'info');
      const res = await fetch('/api/employee/profile/photo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ photo: base64 })
      });
      const data = await res.json();

      if (res.ok && data.success) {
        showToast('Profile photo updated successfully!', 'success');
        await loadProfile();
      } else {
        showToast(data.message || 'Failed to update photo', 'error');
      }
    } catch (err) {
      showToast('Network error while saving photo', 'error');
    }
  };
  reader.readAsDataURL(file);
}

// Edit Contact Info Modal
function openEditProfileModal() {
  if (!currentEmployee) return;

  const phoneInput = document.getElementById('editContactPhone');
  const genderInput = document.getElementById('editContactGender');
  const dobInput = document.getElementById('editContactDob');
  const addressInput = document.getElementById('editContactAddress');

  if (phoneInput) phoneInput.value = currentEmployee.phone || '';
  if (genderInput) genderInput.value = currentEmployee.gender || 'Not Specified';
  if (dobInput) dobInput.value = currentEmployee.dob || '';
  if (addressInput) addressInput.value = currentEmployee.address || '';

  openModal('editContactModal');
}

async function submitEditContact(e) {
  e.preventDefault();
  const btn = document.getElementById('saveContactBtn');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  const updateData = {
    phone: document.getElementById('editContactPhone').value.trim(),
    gender: document.getElementById('editContactGender').value,
    dob: document.getElementById('editContactDob').value || null,
    address: document.getElementById('editContactAddress').value.trim()
  };

  try {
    const res = await fetch('/api/employee/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updateData)
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('Contact details updated successfully!', 'success');
      closeModal('editContactModal');
      await loadProfile();
    } else {
      showToast(data.message || 'Failed to update details', 'error');
    }
  } catch (err) {
    showToast('Network error while saving details', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Update Details';
  }
}

// ================= SECTION 3: ATTENDANCE HISTORY =================
async function loadAttendanceHistory() {
  try {
    const res = await fetch('/api/employee/attendance/history');
    const data = await res.json();
    if (!data.success) return;

    renderAttendanceHistory(data.history || []);
  } catch (err) {
    console.error('Failed to load attendance history:', err);
  }
}

function renderAttendanceHistory(records) {
  const tbody = document.getElementById('historyTableBody');
  if (!tbody) return;

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 20px;">No attendance records found.</td></tr>';
    return;
  }

  tbody.innerHTML = records.map(att => {
    const inStr = att.punchIn ? formatTime(att.punchIn) : '-';
    const outStr = att.punchOut ? formatTime(att.punchOut) : '<span style="color: var(--warning); font-weight: 600;">Present</span>';
    const hoursBadge = att.punchOut
      ? `<span class="badge badge-approved">${escapeHtml(att.formattedWorkHours || '0h 0m')}</span>`
      : `<span class="badge badge-pending">In Office</span>`;

    return `
      <tr>
        <td><strong>${att.workDate}</strong></td>
        <td><strong>${inStr}</strong></td>
        <td>${outStr}</td>
        <td>${hoursBadge}</td>
        <td style="color: #475569; font-weight: 500;">${escapeHtml(att.detailedSummary || '-')}</td>
        <td><span class="badge" style="background:#e0e7ff; color:#3730a3;">${escapeHtml(att.punchMethod || 'FACE')}</span></td>
      </tr>
    `;
  }).join('');
}

function formatTime(timeStr) {
  if (!timeStr) return '-';
  try {
    const parts = timeStr.split(':');
    let h = parseInt(parts[0], 10);
    const m = parts[1];
    const ampm = h >= 12 ? 'PM' : 'AM';
    h = h % 12;
    h = h ? h : 12;
    const hStr = h < 10 ? '0' + h : h;
    return `${hStr}:${m} ${ampm}`;
  } catch (e) {
    return timeStr;
  }
}

// ================= SECTION 4 & 5: LEAVE MANAGEMENT =================
async function handleApplyLeave(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('submitLeaveBtn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Submitting to JV Group...';

  const leaveData = {
    leaveType: document.getElementById('leaveTypeSelect').value,
    fromDate: document.getElementById('leaveFromDate').value,
    toDate: document.getElementById('leaveToDate').value,
    reason: document.getElementById('leaveReason').value.trim()
  };

  try {
    const res = await fetch('/api/employee/leaves', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(leaveData)
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('Leave application submitted successfully.', 'success');
      document.getElementById('applyLeaveForm').reset();
      const todayStr = new Date().toISOString().split('T')[0];
      document.getElementById('leaveFromDate').value = todayStr;
      document.getElementById('leaveToDate').value = todayStr;
      switchTab('my-leaves');
    } else {
      showToast(data.message || 'Failed to submit leave application.', 'error');
    }
  } catch (err) {
    showToast('Network error submitting leave application.', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Submit Leave Application';
  }
}

async function loadMyLeaves() {
  try {
    const res = await fetch('/api/employee/leaves');
    const data = await res.json();
    if (!data.success) return;

    renderMyLeaves(data.leaves || []);
  } catch (err) {
    console.error('Failed to load leave requests:', err);
  }
}

function renderMyLeaves(leaves) {
  const tbody = document.getElementById('myLeavesTableBody');
  if (!tbody) return;

  if (leaves.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 20px;">No leave applications submitted yet.</td></tr>';
    return;
  }

  tbody.innerHTML = leaves.map(lr => {
    let statusBadge = '';
    if (lr.status === 'PENDING') statusBadge = '<span class="badge badge-pending">PENDING</span>';
    else if (lr.status === 'APPROVED') statusBadge = '<span class="badge badge-approved">APPROVED</span>';
    else if (lr.status === 'REJECTED') statusBadge = '<span class="badge badge-rejected">REJECTED</span>';

    const appliedStr = lr.appliedAt ? lr.appliedAt.replace('T', ' ').substring(0, 16) : '-';
    const noteStr = lr.adminNote ? `<span style="font-weight: 500; color: #1e293b;">${escapeHtml(lr.adminNote)}</span>` : '<span style="color: var(--text-muted);">-</span>';

    return `
      <tr>
        <td><strong>${escapeHtml(lr.leaveType)}</strong></td>
        <td>${lr.fromDate}</td>
        <td>${lr.toDate}</td>
        <td style="max-width: 200px; white-space: normal;">${escapeHtml(lr.reason)}</td>
        <td>${statusBadge}</td>
        <td>${noteStr}</td>
        <td>${appliedStr}</td>
      </tr>
    `;
  }).join('');
}

// ================= SECTION 6: NOTIFICATIONS =================
async function loadNotifications() {
  try {
    const res = await fetch('/api/employee/notifications');
    const data = await res.json();
    if (!data.success) return;

    const notifs = data.notifications || [];
    const unreadCount = data.unreadCount || 0;

    const badge = document.getElementById('notifBadge');
    if (badge) {
      if (unreadCount > 0) {
        badge.textContent = unreadCount;
        badge.style.display = 'inline-block';
      } else {
        badge.style.display = 'none';
      }
    }

    renderNotifications(notifs);
  } catch (err) {
    console.error('Failed to load notifications:', err);
  }
}

function renderNotifications(notifs) {
  const container = document.getElementById('notificationsContainer');
  if (!container) return;

  if (notifs.length === 0) {
    container.innerHTML = '<p style="color: var(--text-muted); text-align: center; padding: 20px;">No notifications to display.</p>';
    return;
  }

  container.innerHTML = notifs.map(n => {
    const isUnread = !n.isRead;
    const timeStr = n.createdAt ? n.createdAt.replace('T', ' ').substring(0, 16) : '';
    const readBtn = isUnread
      ? `<button class="btn btn-secondary btn-sm" onclick="markNotificationRead(${n.id})">Mark as Read</button>`
      : `<span class="badge badge-inactive">Read</span>`;

    return `
      <div class="notification-card ${isUnread ? 'unread' : ''}">
        <div class="notification-content">
          <h4>${escapeHtml(n.title)}</h4>
          <p>${escapeHtml(n.message)}</p>
          <div class="notif-time">${timeStr}</div>
        </div>
        <div>${readBtn}</div>
      </div>
    `;
  }).join('');
}

async function markNotificationRead(id) {
  try {
    const res = await fetch(`/api/employee/notifications/${id}/read`, { method: 'PATCH' });
    if (res.ok) {
      loadNotifications();
    }
  } catch (err) {
    console.error('Failed to mark notification read:', err);
  }
}

async function markAllNotificationsRead() {
  try {
    const res = await fetch('/api/employee/notifications/read-all', { method: 'PATCH' });
    if (res.ok) {
      showToast('All notifications marked as read', 'success');
      loadNotifications();
    }
  } catch (err) {
    showToast('Failed to mark all as read', 'error');
  }
}

// ================= SECTION 7: CAMERA ATTENDANCE (Requirements 6, 7 & 8) =================
let attendanceCameraStream = null;
let currentPunchType = 'IN'; // 'IN' or 'OUT'
let capturedPunchPhotoBase64 = null;

async function openAttendanceCamera(type) {
  currentPunchType = type;
  capturedPunchPhotoBase64 = null;

  const titleEl = document.getElementById('cameraModalTitle');
  const subEl = document.getElementById('cameraModalSubtitle');
  const videoEl = document.getElementById('cameraVideo');
  const guideEl = document.getElementById('cameraGuideOverlay');
  const capturedImgEl = document.getElementById('cameraCapturedImg');
  const errorEl = document.getElementById('cameraErrorMsg');
  const btnCapture = document.getElementById('btnCapturePhoto');
  const btnRetake = document.getElementById('btnRetakePhoto');
  const btnConfirm = document.getElementById('btnConfirmPunch');
  const noteEl = document.getElementById('cameraStatusNote');

  if (titleEl) {
    titleEl.textContent = type === 'IN' ? '📷 Punch In – Verification Photo' : '📷 Punch Out – Verification Photo';
  }
  if (subEl) {
    subEl.textContent = type === 'IN'
      ? 'Position your face inside the frame to record your daily Punch In'
      : 'Position your face inside the frame to record your daily Punch Out';
  }

  // Reset visual elements
  if (videoEl) videoEl.style.display = 'block';
  if (guideEl) guideEl.style.display = 'block';
  if (capturedImgEl) {
    capturedImgEl.style.display = 'none';
    capturedImgEl.src = '';
  }
  if (errorEl) {
    errorEl.style.display = 'none';
    errorEl.textContent = '';
  }
  if (btnCapture) {
    btnCapture.style.display = 'inline-block';
    btnCapture.disabled = false;
  }
  if (btnRetake) btnRetake.style.display = 'none';
  if (btnConfirm) {
    btnConfirm.style.display = 'none';
    btnConfirm.disabled = false;
    btnConfirm.textContent = type === 'IN' ? '✅ Confirm Punch In' : '✅ Confirm Punch Out';
  }
  if (noteEl) {
    noteEl.textContent = 'Please look directly at the camera and ensure good lighting.';
    noteEl.style.color = 'var(--text-muted)';
  }

  openModal('attendanceCameraModal');

  // Stop any previous stream
  if (attendanceCameraStream) {
    attendanceCameraStream.getTracks().forEach(t => t.stop());
    attendanceCameraStream = null;
  }

  // Request user camera
  try {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      throw new Error('Camera access is not supported by this browser. Please use Chrome, Edge, or Firefox.');
    }

    attendanceCameraStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 640 },
        height: { ideal: 480 },
        facingMode: 'user'
      },
      audio: false
    });

    if (videoEl) {
      videoEl.srcObject = attendanceCameraStream;
      await videoEl.play();
    }
  } catch (err) {
    console.error('Camera access error:', err);
    if (errorEl) {
      errorEl.textContent = '⚠️ Unable to access camera: ' + (err.message || 'Permission denied or no webcam found.');
      errorEl.style.display = 'block';
    }
    if (guideEl) guideEl.style.display = 'none';
    if (btnCapture) btnCapture.disabled = true;
    if (noteEl) {
      noteEl.textContent = 'Camera permission required. Please allow camera access in browser settings.';
      noteEl.style.color = '#dc2626';
    }
  }
}

function captureAttendancePhoto() {
  const videoEl = document.getElementById('cameraVideo');
  const canvasEl = document.getElementById('cameraCanvas');
  const capturedImgEl = document.getElementById('cameraCapturedImg');
  const guideEl = document.getElementById('cameraGuideOverlay');
  const btnCapture = document.getElementById('btnCapturePhoto');
  const btnRetake = document.getElementById('btnRetakePhoto');
  const btnConfirm = document.getElementById('btnConfirmPunch');
  const noteEl = document.getElementById('cameraStatusNote');

  if (!videoEl || !canvasEl || !capturedImgEl) return;

  const width = videoEl.videoWidth || 640;
  const height = videoEl.videoHeight || 480;

  canvasEl.width = width;
  canvasEl.height = height;
  const ctx = canvasEl.getContext('2d');
  ctx.drawImage(videoEl, 0, 0, width, height);

  capturedPunchPhotoBase64 = canvasEl.toDataURL('image/jpeg', 0.85);

  // Show captured image preview and hide video
  capturedImgEl.src = capturedPunchPhotoBase64;
  capturedImgEl.style.display = 'block';
  videoEl.style.display = 'none';
  if (guideEl) guideEl.style.display = 'none';

  // Toggle buttons
  if (btnCapture) btnCapture.style.display = 'none';
  if (btnRetake) btnRetake.style.display = 'inline-block';
  if (btnConfirm) {
    btnConfirm.style.display = 'inline-block';
    btnConfirm.textContent = currentPunchType === 'IN' ? '✅ Confirm Punch In' : '✅ Confirm Punch Out';
  }
  if (noteEl) {
    noteEl.textContent = 'Photo captured successfully! Review your photo and click Confirm to record attendance.';
    noteEl.style.color = '#059669';
  }
}

function retakeAttendancePhoto() {
  capturedPunchPhotoBase64 = null;

  const videoEl = document.getElementById('cameraVideo');
  const guideEl = document.getElementById('cameraGuideOverlay');
  const capturedImgEl = document.getElementById('cameraCapturedImg');
  const btnCapture = document.getElementById('btnCapturePhoto');
  const btnRetake = document.getElementById('btnRetakePhoto');
  const btnConfirm = document.getElementById('btnConfirmPunch');
  const noteEl = document.getElementById('cameraStatusNote');

  if (capturedImgEl) {
    capturedImgEl.style.display = 'none';
    capturedImgEl.src = '';
  }
  if (videoEl) videoEl.style.display = 'block';
  if (guideEl) guideEl.style.display = 'block';

  if (btnCapture) {
    btnCapture.style.display = 'inline-block';
    btnCapture.disabled = false;
  }
  if (btnRetake) btnRetake.style.display = 'none';
  if (btnConfirm) btnConfirm.style.display = 'none';

  if (noteEl) {
    noteEl.textContent = 'Please look directly at the camera and ensure good lighting.';
    noteEl.style.color = 'var(--text-muted)';
  }
}

function closeCameraModal() {
  if (attendanceCameraStream) {
    attendanceCameraStream.getTracks().forEach(track => track.stop());
    attendanceCameraStream = null;
  }
  const videoEl = document.getElementById('cameraVideo');
  if (videoEl) videoEl.srcObject = null;

  capturedPunchPhotoBase64 = null;
  closeModal('attendanceCameraModal');
}

async function confirmAttendancePunch() {
  if (!capturedPunchPhotoBase64) {
    showToast('Please capture your photo before confirming punch.', 'error');
    return;
  }

  const btnConfirm = document.getElementById('btnConfirmPunch');
  if (btnConfirm) {
    btnConfirm.disabled = true;
    btnConfirm.textContent = 'Recording Attendance...';
  }

  const endpoint = currentPunchType === 'IN'
    ? '/api/employee/attendance/punch-in'
    : '/api/employee/attendance/punch-out';

  try {
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ photo: capturedPunchPhotoBase64 })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast(data.message || (currentPunchType === 'IN' ? 'Punched in successfully!' : 'Punched out successfully!'), 'success');
      closeCameraModal();
      await loadTodayStatus();
      await loadAttendanceHistory();
    } else {
      showToast(data.message || 'Failed to record attendance.', 'error');
      if (btnConfirm) {
        btnConfirm.disabled = false;
        btnConfirm.textContent = currentPunchType === 'IN' ? '✅ Confirm Punch In' : '✅ Confirm Punch Out';
      }
    }
  } catch (err) {
    console.error('Punch request error:', err);
    showToast('Network error while recording attendance.', 'error');
    if (btnConfirm) {
      btnConfirm.disabled = false;
      btnConfirm.textContent = currentPunchType === 'IN' ? '✅ Confirm Punch In' : '✅ Confirm Punch Out';
    }
  }
}

// ================= SECTION 8: CHANGE PASSWORD (Requirement 11) =================
async function submitChangePassword(e) {
  e.preventDefault();
  const currentPass = document.getElementById('currentPass').value;
  const newPass = document.getElementById('newPass').value;
  const confirmNewPass = document.getElementById('confirmNewPass').value;
  const btn = document.getElementById('savePasswordBtn');

  if (!currentPass || !newPass || !confirmNewPass) {
    showToast('Please fill in all password fields.', 'error');
    return;
  }

  if (newPass.length < 4) {
    showToast('New password must be at least 4 characters long.', 'error');
    return;
  }

  if (newPass !== confirmNewPass) {
    showToast('New passwords do not match. Please re-enter.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Updating Password...';

  try {
    const res = await fetch('/api/employee/profile/change-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        currentPassword: currentPass,
        newPassword: newPass
      })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('Password updated successfully! Keep your new credentials safe.', 'success');
      document.getElementById('changePasswordForm').reset();
      closeModal('changePasswordModal');
    } else {
      showToast(data.message || 'Failed to update password.', 'error');
    }
  } catch (err) {
    console.error('Change password error:', err);
    showToast('Network error while updating password.', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Update Password';
  }
}

function togglePasswordVisibility(inputId) {
  const input = document.getElementById(inputId);
  if (!input) return;
  input.type = input.type === 'password' ? 'text' : 'password';
}

// Modal Helpers with scroll lock & backdrop dismissal
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.classList.add('modal-open');
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
  }
  // Remove modal-open if no modals are active
  if (!document.querySelector('.modal-overlay.active')) {
    document.body.classList.remove('modal-open');
  }
}

// Global modal click backdrop and ESC key handlers
window.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    if (e.target.id === 'attendanceCameraModal') {
      closeCameraModal();
    } else {
      closeModal(e.target.id);
    }
  }
});

window.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    const activeModal = document.querySelector('.modal-overlay.active');
    if (activeModal) {
      if (activeModal.id === 'attendanceCameraModal') {
        closeCameraModal();
      } else {
        closeModal(activeModal.id);
      }
    }
  }
});

// Utility: Escape HTML
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

