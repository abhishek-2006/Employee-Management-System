/**
 * JV Group Employee Management System (JV-GEMS)
 * Admin Portal JavaScript
 * Full implementation with real-time Search, Filters, Photo Uploads, and Modals
 */

let allEmployeesCache = [];
let pendingDeleteEmpId = null;
let currentReviewLeaveId = null;
let currentAddPhotoBase64 = null;
let currentEditPhotoBase64 = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
  const isAuth = await checkAdminAuth();
  if (!isAuth) return;

  // Set default join date to today
  const todayStr = new Date().toISOString().split('T')[0];
  const joinDateInput = document.getElementById('addJoinDate');
  if (joinDateInput) joinDateInput.value = todayStr;

  // Load all data
  await loadDashboardData();
  await loadEmployees();
  loadAttendance();
  loadLeaveRequests();
});

// Check Admin Authentication
async function checkAdminAuth() {
  try {
    const res = await fetch('/api/admin/auth/status');
    const data = await res.json();
    if (!data.authenticated) {
      window.location.href = 'index.html?role=admin';
      return false;
    }
    // Update admin info display
    if (data.admin && data.admin.fullName) {
      const nameEl = document.getElementById('adminNameDisplay');
      const avatarEl = document.getElementById('adminAvatar');
      if (nameEl) nameEl.textContent = data.admin.fullName;
      if (avatarEl) avatarEl.textContent = data.admin.fullName.charAt(0).toUpperCase();
    }
    return true;
  } catch (err) {
    window.location.href = 'index.html?role=admin';
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

// Logout
async function handleLogout() {
  if (!confirm('Are you sure you want to log out from JV-GEMS Admin Portal?')) return;
  try {
    await fetch('/api/admin/auth/logout', { method: 'POST' });
  } finally {
    window.location.href = 'index.html?role=admin';
  }
}

// Switch Tab
function switchTab(tabName) {
  // Close mobile sidebar if open
  const sidebar = document.getElementById('appSidebar');
  if (sidebar) sidebar.classList.remove('mobile-open');

  // Update menu links
  const links = document.querySelectorAll('.menu-link');
  links.forEach(l => l.classList.remove('active'));

  // Update tab panes
  const panes = document.querySelectorAll('.tab-pane');
  panes.forEach(p => p.classList.remove('active'));

  const targetPane = document.getElementById(`pane-${tabName}`);
  if (targetPane) targetPane.classList.add('active');

  // Activate menu link
  const activeLink = Array.from(links).find(l => l.getAttribute('onclick')?.includes(tabName));
  if (activeLink) activeLink.classList.add('active');

  // Update section title
  const titleEl = document.getElementById('currentSectionTitle');
  if (titleEl) {
    const titles = {
      'dashboard': 'Admin Dashboard',
      'employees': 'Employee Management',
      'attendance': 'Attendance Records',
      'leaves': 'Leave Management',
      'reports': 'Workforce Reports',
      'settings': 'Enterprise Settings'
    };
    titleEl.textContent = titles[tabName] || 'JV-GEMS Admin';
  }

  // Refresh data for the chosen tab
  if (tabName === 'dashboard') loadDashboardData();
  if (tabName === 'employees') loadEmployees();
  if (tabName === 'attendance') loadAttendance();
  if (tabName === 'leaves') loadLeaveRequests();
  if (tabName === 'reports') renderReports();
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

// ================= SECTION 1: DASHBOARD =================
async function loadDashboardData() {
  try {
    const res = await fetch('/api/admin/dashboard/stats');
    const data = await res.json();
    if (data.success && data.stats) {
      const stats = data.stats;
      document.getElementById('statTotalEmployees').textContent = stats.totalEmployees || 0;
      document.getElementById('statActiveEmployees').textContent = stats.activeEmployees || 0;
      document.getElementById('statInactiveEmployees').textContent = stats.inactiveEmployees || 0;
      document.getElementById('statDepartments').textContent = stats.departmentsCount || 0;
      document.getElementById('statTodayPresent').textContent = stats.todayPresent || 0;
      document.getElementById('statPendingLeaves').textContent = stats.pendingLeaveRequests || 0;

      // Update sidebar badge for pending leaves
      const pendingBadge = document.getElementById('sidebarPendingLeavesCount');
      if (pendingBadge) {
        if (stats.pendingLeaveRequests > 0) {
          pendingBadge.textContent = stats.pendingLeaveRequests;
          pendingBadge.style.display = 'inline-block';
        } else {
          pendingBadge.style.display = 'none';
        }
      }
    }
  } catch (err) {
    console.error('Failed to load dashboard stats:', err);
  }
}

// ================= SECTION 2: EMPLOYEES =================
async function loadEmployees() {
  try {
    const res = await fetch('/api/admin/employees');
    const data = await res.json();
    if (!data.success) return;

    allEmployeesCache = data.employees || [];
    populateDynamicFilterDropdowns(allEmployeesCache);
    handleEmployeeFilterChange();
    renderQuickEmployees(allEmployeesCache);
    populateAttendanceEmployeeFilter(allEmployeesCache);
    renderReports();
  } catch (err) {
    console.error('Failed to load employees:', err);
  }
}

// Populate filter dropdowns dynamically from existing employee data
function populateDynamicFilterDropdowns(employees) {
  const deptSelect = document.getElementById('empFilterDept');
  const desigSelect = document.getElementById('empFilterDesig');
  if (!deptSelect || !desigSelect) return;

  const currentDept = deptSelect.value;
  const currentDesig = desigSelect.value;

  const depts = new Set();
  const desigs = new Set();

  employees.forEach(e => {
    if (e.department && e.department.trim()) depts.add(e.department.trim());
    if (e.designation && e.designation.trim()) desigs.add(e.designation.trim());
  });

  deptSelect.innerHTML = '<option value="">All Departments</option>' +
    Array.from(depts).sort().map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
  deptSelect.value = currentDept;

  desigSelect.innerHTML = '<option value="">All Designations</option>' +
    Array.from(desigs).sort().map(d => `<option value="${escapeHtml(d)}">${escapeHtml(d)}</option>`).join('');
  desigSelect.value = currentDesig;
}

// Modern Search & Filter System (Requirement 8)
function handleEmployeeFilterChange() {
  const searchTerm = (document.getElementById('employeeSearchInput')?.value || '').trim().toLowerCase();
  const selectedDept = document.getElementById('empFilterDept')?.value || '';
  const selectedDesig = document.getElementById('empFilterDesig')?.value || '';
  const selectedStatus = document.getElementById('empFilterStatus')?.value || 'ALL';

  const filtered = allEmployeesCache.filter(emp => {
    // Search query matches: Name, Employee ID, Email, Phone
    if (searchTerm) {
      const matchName = emp.fullName && emp.fullName.toLowerCase().includes(searchTerm);
      const matchCode = emp.employeeCode && emp.employeeCode.toLowerCase().includes(searchTerm);
      const matchEmail = emp.email && emp.email.toLowerCase().includes(searchTerm);
      const matchPhone = emp.phone && emp.phone.toLowerCase().includes(searchTerm);
      if (!matchName && !matchCode && !matchEmail && !matchPhone) return false;
    }

    // Filter: Department
    if (selectedDept && emp.department !== selectedDept) {
      return false;
    }

    // Filter: Designation
    if (selectedDesig && emp.designation !== selectedDesig) {
      return false;
    }

    // Filter: Status
    if (selectedStatus !== 'ALL') {
      if (emp.status !== selectedStatus) return false;
    }

    return true;
  });

  // Update count text
  const countEl = document.getElementById('employeeFilterCountText');
  if (countEl) {
    countEl.textContent = `Showing ${filtered.length} of ${allEmployeesCache.length} employees`;
  }

  renderEmployeesTable(filtered);
}

function resetEmployeeFilters() {
  if (document.getElementById('employeeSearchInput')) document.getElementById('employeeSearchInput').value = '';
  if (document.getElementById('empFilterDept')) document.getElementById('empFilterDept').value = '';
  if (document.getElementById('empFilterDesig')) document.getElementById('empFilterDesig').value = '';
  if (document.getElementById('empFilterStatus')) document.getElementById('empFilterStatus').value = 'ALL';
  handleEmployeeFilterChange();
}

// Render Employee Table with Circular Photos (Requirement 5 & 7)
function renderEmployeesTable(employees) {
  const tbody = document.getElementById('employeesTableBody');
  if (!tbody) return;

  if (employees.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 24px;">No employees match the search or filter criteria.</td></tr>';
    return;
  }

  tbody.innerHTML = employees.map(emp => {
    const isActive = emp.status === 'ACTIVE';
    const statusBadge = isActive
      ? `<span class="badge badge-active"><span class="status-dot"></span> Active</span>`
      : `<span class="badge badge-inactive">Inactive</span>`;

    const toggleBtnTitle = isActive ? 'Deactivate account' : 'Activate account';
    const toggleIcon = isActive ? '⏸️' : '▶️';

    // Circular Photo or Monogram Initials Fallback
    const photoHtml = buildEmployeeAvatarHtml(emp.photo, emp.fullName, 42);

    return `
      <tr>
        <td>
          <div class="emp-photo-cell">
            ${photoHtml}
          </div>
        </td>
        <td><strong>${escapeHtml(emp.employeeCode)}</strong></td>
        <td>
          <span style="font-weight: 600; color: var(--text-dark);">${escapeHtml(emp.fullName)}</span>
        </td>
        <td><a href="mailto:${escapeHtml(emp.email)}" style="color: var(--primary); text-decoration: none;">${escapeHtml(emp.email)}</a></td>
        <td>${escapeHtml(emp.phone || '-')}</td>
        <td><span class="badge" style="background: #f1f5f9; color: #334155;">${escapeHtml(emp.department || '-')}</span></td>
        <td>${escapeHtml(emp.designation || '-')}</td>
        <td>${statusBadge}</td>
        <td>
          <div class="action-btn-group">
            <button class="icon-action-btn" title="View Full Profile" onclick="viewEmployeeProfile(${emp.id})">
              <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
            </button>
            <button class="icon-action-btn" title="Edit Employee" onclick="openEditEmployeeModal(${emp.id})">
              <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
            </button>
            <button class="icon-action-btn" title="${toggleBtnTitle}" onclick="toggleEmployeeStatus(${emp.id}, '${isActive ? 'INACTIVE' : 'ACTIVE'}')">
              <span>${toggleIcon}</span>
            </button>
            <button class="icon-action-btn delete" title="Delete Employee" onclick="openDeleteEmployeeModal(${emp.id})">
              <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// Render Quick Employees on Dashboard
function renderQuickEmployees(employees) {
  const tbody = document.getElementById('quickEmployeesBody');
  if (!tbody) return;

  const preview = employees.slice(0, 5);
  if (preview.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 18px;">No employees yet.</td></tr>';
    return;
  }

  tbody.innerHTML = preview.map(emp => {
    const isActive = emp.status === 'ACTIVE';
    const statusBadge = isActive
      ? `<span class="badge badge-active"><span class="status-dot"></span> Active</span>`
      : `<span class="badge badge-inactive">Inactive</span>`;
    const faceBadge = emp.faceEnrolled
      ? `<span class="badge badge-approved">Enrolled</span>`
      : `<span class="badge badge-inactive">Pending</span>`;

    const photoHtml = buildEmployeeAvatarHtml(emp.photo, emp.fullName, 36);

    return `
      <tr>
        <td>
          <div class="emp-photo-cell" style="width: 36px; height: 36px;">
            ${photoHtml}
          </div>
        </td>
        <td><strong>${escapeHtml(emp.employeeCode)}</strong></td>
        <td>${escapeHtml(emp.fullName)}</td>
        <td>${escapeHtml(emp.department || '-')}</td>
        <td>${escapeHtml(emp.designation || '-')}</td>
        <td>${statusBadge}</td>
        <td>${faceBadge}</td>
      </tr>
    `;
  }).join('');
}

// Avatar Helper (Requirement 7)
function buildEmployeeAvatarHtml(photoUrl, fullName, size = 42) {
  if (photoUrl && photoUrl.trim()) {
    return `<img src="${photoUrl}" alt="${escapeHtml(fullName)}" onerror="this.onerror=null; this.parentElement.innerHTML='<div class=\\'emp-avatar-initials\\'>${getInitials(fullName)}</div>';">`;
  }
  return `<div class="emp-avatar-initials">${getInitials(fullName)}</div>`;
}

function getInitials(name) {
  if (!name) return 'JV';
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function populateAttendanceEmployeeFilter(employees) {
  const select = document.getElementById('attFilterEmployee');
  if (!select) return;

  const currentVal = select.value;
  select.innerHTML = '<option value="">All Employees</option>' +
    employees.map(e => `<option value="${e.id}">${escapeHtml(e.employeeCode)} - ${escapeHtml(e.fullName)}</option>`).join('');
  select.value = currentVal;
}

// Handle Photo Upload with FileReader & Preview (Requirement 7)
function handlePhotoUpload(input, mode) {
  if (!input.files || !input.files[0]) return;
  const file = input.files[0];

  if (!file.type.startsWith('image/')) {
    showToast('Please select a valid image file', 'error');
    return;
  }

  // 5MB limit
  if (file.size > 5 * 1024 * 1024) {
    showToast('Image file size must be under 5MB', 'error');
    return;
  }

  const reader = new FileReader();
  reader.onload = function(e) {
    const base64 = e.target.result;
    if (mode === 'add') {
      currentAddPhotoBase64 = base64;
      document.getElementById('addPhotoPlaceholder').style.display = 'none';
      const img = document.getElementById('addPhotoPreviewImg');
      img.src = base64;
      img.style.display = 'block';
      document.getElementById('addPhotoRemoveBtn').style.display = 'inline-block';
    } else if (mode === 'edit') {
      currentEditPhotoBase64 = base64;
      document.getElementById('editPhotoPlaceholder').style.display = 'none';
      const img = document.getElementById('editPhotoPreviewImg');
      img.src = base64;
      img.style.display = 'block';
      document.getElementById('editPhotoRemoveBtn').style.display = 'inline-block';
    }
  };
  reader.readAsDataURL(file);
}

function removeUploadedPhoto(mode) {
  if (mode === 'add') {
    currentAddPhotoBase64 = null;
    document.getElementById('addPhotoInput').value = '';
    document.getElementById('addPhotoPreviewImg').style.display = 'none';
    document.getElementById('addPhotoPlaceholder').style.display = 'inline-block';
    document.getElementById('addPhotoRemoveBtn').style.display = 'none';
  } else if (mode === 'edit') {
    currentEditPhotoBase64 = '';
    document.getElementById('editPhotoInput').value = '';
    document.getElementById('editPhotoPreviewImg').style.display = 'none';
    document.getElementById('editPhotoPlaceholder').style.display = 'inline-block';
    document.getElementById('editPhotoRemoveBtn').style.display = 'none';
  }
}

// Add Employee
function openAddEmployeeModal() {
  document.getElementById('addEmployeeForm').reset();
  removeUploadedPhoto('add');
  const todayStr = new Date().toISOString().split('T')[0];
  document.getElementById('addJoinDate').value = todayStr;
  openModal('addEmployeeModal');
}

async function submitAddEmployee(e) {
  e.preventDefault();
  const btn = document.getElementById('saveEmployeeBtn');

  const password = document.getElementById('addPassword').value;
  const confirmPassword = document.getElementById('addConfirmPassword').value;

  if (password !== confirmPassword) {
    showToast('❌ Passwords do not match. Please verify.', 'error');
    return;
  }

  if (password.length < 4) {
    showToast('❌ Password must be at least 4 characters long.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Creating Employee...';

  const empCode = document.getElementById('addEmpCode').value.trim();
  const rawUsername = document.getElementById('addUsername').value.trim();

  const employeeData = {
    employeeCode: empCode,
    username: rawUsername || empCode,
    fullName: document.getElementById('addFullName').value.trim(),
    email: document.getElementById('addEmail').value.trim(),
    phone: document.getElementById('addPhone').value.trim(),
    department: document.getElementById('addDepartment').value,
    designation: document.getElementById('addDesignation').value.trim(),
    gender: document.getElementById('addGender').value,
    dob: document.getElementById('addDob').value || null,
    salary: parseFloat(document.getElementById('addSalary').value) || 0.0,
    joinDate: document.getElementById('addJoinDate').value,
    password: password,
    address: document.getElementById('addAddress').value.trim(),
    photo: currentAddPhotoBase64,
    status: document.getElementById('addStatus').value || 'ACTIVE'
  };

  try {
    const res = await fetch('/api/admin/employees', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(employeeData)
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('✅ Employee created successfully with login credentials!', 'success');
      closeModal('addEmployeeModal');
      await loadEmployees();
      await loadDashboardData();
    } else {
      showToast('❌ ' + (data.message || 'Failed to add employee'), 'error');
    }
  } catch (err) {
    showToast('❌ Network error while saving employee', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Create Employee';
  }
}

// Edit Employee
function openEditEmployeeModal(id) {
  const emp = allEmployeesCache.find(e => e.id === id);
  if (!emp) return;

  document.getElementById('editEmpId').value = emp.id;
  document.getElementById('editEmpCode').value = emp.employeeCode;
  document.getElementById('editFullName').value = emp.fullName;
  document.getElementById('editEmail').value = emp.email;
  document.getElementById('editPhone').value = emp.phone || '';
  document.getElementById('editDepartment').value = emp.department || 'Engineering';
  document.getElementById('editDesignation').value = emp.designation || '';
  document.getElementById('editGender').value = emp.gender || 'Not Specified';
  document.getElementById('editDob').value = emp.dob || '';
  document.getElementById('editJoinDate').value = emp.joinDate || '';
  document.getElementById('editSalary').value = emp.salary || '';
  document.getElementById('editAddress').value = emp.address || '';

  // Photo preview
  currentEditPhotoBase64 = null;
  const placeholder = document.getElementById('editPhotoPlaceholder');
  const img = document.getElementById('editPhotoPreviewImg');
  const removeBtn = document.getElementById('editPhotoRemoveBtn');

  if (emp.photo) {
    img.src = emp.photo;
    img.style.display = 'block';
    placeholder.style.display = 'none';
    removeBtn.style.display = 'inline-block';
  } else {
    img.style.display = 'none';
    placeholder.style.display = 'inline-block';
    removeBtn.style.display = 'none';
  }

  openModal('editEmployeeModal');
}

async function submitEditEmployee(e) {
  e.preventDefault();
  const id = document.getElementById('editEmpId').value;
  const btn = document.getElementById('updateEmployeeBtn');
  btn.disabled = true;
  btn.textContent = 'Saving Changes...';

  const employeeData = {
    fullName: document.getElementById('editFullName').value.trim(),
    email: document.getElementById('editEmail').value.trim(),
    phone: document.getElementById('editPhone').value.trim(),
    department: document.getElementById('editDepartment').value,
    designation: document.getElementById('editDesignation').value.trim(),
    gender: document.getElementById('editGender').value,
    dob: document.getElementById('editDob').value || null,
    salary: parseFloat(document.getElementById('editSalary').value) || 0.0,
    joinDate: document.getElementById('editJoinDate').value,
    address: document.getElementById('editAddress').value.trim(),
    photo: currentEditPhotoBase64 !== null ? currentEditPhotoBase64 : undefined
  };

  try {
    const res = await fetch(`/api/admin/employees/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(employeeData)
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('Employee updated successfully!', 'success');
      closeModal('editEmployeeModal');
      await loadEmployees();
    } else {
      showToast(data.message || 'Failed to update employee', 'error');
    }
  } catch (err) {
    showToast('Network error while updating employee', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Changes';
  }
}

// Toggle Employee Status
async function toggleEmployeeStatus(id, newStatus) {
  const actionName = newStatus === 'ACTIVE' ? 'activate' : 'deactivate';
  if (!confirm(`Are you sure you want to ${actionName} this JV Group employee account?`)) return;

  try {
    const res = await fetch(`/api/admin/employees/${id}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast(`Employee ${actionName}d successfully`, 'success');
      await loadEmployees();
      await loadDashboardData();
    } else {
      showToast(data.message || 'Could not update status', 'error');
    }
  } catch (err) {
    showToast('Error updating employee status', 'error');
  }
}

// Delete Employee Modal & Execution (Requirement 5)
function openDeleteEmployeeModal(id) {
  const emp = allEmployeesCache.find(e => e.id === id);
  if (!emp) return;

  pendingDeleteEmpId = id;
  document.getElementById('deleteEmpNameSpan').textContent = emp.fullName;
  document.getElementById('deleteEmpCodeSpan').textContent = `${emp.employeeCode} (${emp.email})`;
  openModal('deleteEmployeeModal');
}

async function confirmDeleteEmployee() {
  if (!pendingDeleteEmpId) return;

  const btn = document.getElementById('confirmDeleteEmpBtn');
  btn.disabled = true;
  btn.textContent = 'Deleting...';

  try {
    const res = await fetch(`/api/admin/employees/${pendingDeleteEmpId}`, {
      method: 'DELETE'
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast('Employee permanently deleted', 'success');
      closeModal('deleteEmployeeModal');
      await loadEmployees();
      await loadDashboardData();
    } else {
      showToast(data.message || 'Could not delete employee', 'error');
    }
  } catch (err) {
    showToast('Network error while deleting employee', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Permanently Delete';
    pendingDeleteEmpId = null;
  }
}

// Full Employee Details Modal (Requirement 6)
async function viewEmployeeProfile(id) {
  try {
    const res = await fetch(`/api/admin/employees/${id}/details`);
    const data = await res.json();

    if (!data.success || !data.data || !data.data.employee) {
      showToast('Employee information not found', 'error');
      return;
    }

    const emp = data.data.employee;
    const attendances = data.data.attendances || [];
    const leaves = data.data.leaves || [];

    // Header Display
    document.getElementById('viewFullNameDisplay').textContent = emp.fullName;
    document.getElementById('viewEmpCodeDisplay').textContent = `${emp.employeeCode} (ID: ${emp.id})`;
    document.getElementById('viewDeptDesigDisplay').textContent = `${emp.department || 'General'} \u2022 ${emp.designation || 'Staff'}`;

    // Large Avatar Photo
    const heroInitials = document.getElementById('viewHeroInitials');
    const heroImg = document.getElementById('viewHeroImg');
    if (emp.photo) {
      heroImg.src = emp.photo;
      heroImg.style.display = 'block';
      heroInitials.style.display = 'none';
    } else {
      heroImg.style.display = 'none';
      heroInitials.textContent = getInitials(emp.fullName);
      heroInitials.style.display = 'inline-block';
    }

    const isActive = emp.status === 'ACTIVE';
    document.getElementById('viewStatusBadgeDisplay').innerHTML = isActive
      ? '<span class="badge badge-active"><span class="status-dot"></span> Active</span>'
      : '<span class="badge badge-inactive">Inactive</span>';

    currentViewedEmpId = emp.id;
    currentViewedEmpName = emp.fullName;
    currentViewedEmpCode = emp.employeeCode;
    const userEl = document.getElementById('viewUsernameVal');
    if (userEl) userEl.textContent = emp.username || emp.employeeCode;

    // Personal Information
    document.getElementById('viewFullName').textContent = emp.fullName;
    document.getElementById('viewEmpCode').textContent = emp.employeeCode;
    document.getElementById('viewGender').textContent = emp.gender || 'Not Specified';
    document.getElementById('viewDob').textContent = emp.dob || 'Not provided';
    document.getElementById('viewFaceStatus').innerHTML = emp.faceEnrolled
      ? '<span class="badge badge-approved">Biometrics Enrolled</span>'
      : '<span class="badge badge-inactive">Not Enrolled</span>';

    // Contact Information
    document.getElementById('viewEmail').textContent = emp.email;
    document.getElementById('viewPhone').textContent = emp.phone || 'Not provided';
    document.getElementById('viewAddress').textContent = emp.address || 'No residential address on file';

    // Job Information
    document.getElementById('viewDepartment').textContent = emp.department || 'Not assigned';
    document.getElementById('viewDesignation').textContent = emp.designation || 'Not assigned';
    document.getElementById('viewJoinDate').textContent = emp.joinDate || 'Not specified';
    document.getElementById('viewSalary').textContent = emp.salary ? `₹${parseFloat(emp.salary).toLocaleString('en-IN')}` : 'Confidential / Not set';
    document.getElementById('viewStatus').innerHTML = isActive
      ? '<span class="badge badge-active">Active</span>'
      : '<span class="badge badge-inactive">Inactive</span>';

    // Attendance & Leave Stats
    document.getElementById('viewAttTotalCount').textContent = `${attendances.length} records`;
    document.getElementById('viewLeaveTotalCount').textContent = `${leaves.length} applications`;
    document.getElementById('viewPendingLeaveCount').textContent = `${data.data.pendingLeaveCount || 0} pending`;

    // Attendance History Table
    const attBody = document.getElementById('viewEmployeeAttendanceBody');
    if (attendances.length === 0) {
      attBody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No attendance records found for this employee.</td></tr>';
    } else {
      attBody.innerHTML = attendances.slice(0, 10).map(att => {
        return `
          <tr>
            <td>${att.workDate}</td>
            <td><strong>${att.punchIn ? formatTime(att.punchIn) : '-'}</strong></td>
            <td>${att.punchOut ? formatTime(att.punchOut) : '<span style="color: var(--warning);">In Office</span>'}</td>
            <td><span class="badge ${att.punchOut ? 'badge-approved' : 'badge-pending'}">${att.formattedWorkHours || '-'}</span></td>
            <td><span class="badge badge-face">${escapeHtml(att.punchMethod || 'FACE')}</span></td>
          </tr>
        `;
      }).join('');
    }

    // Leave History Table
    const leaveBody = document.getElementById('viewEmployeeLeavesBody');
    if (leaves.length === 0) {
      leaveBody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No leave requests found for this employee.</td></tr>';
    } else {
      leaveBody.innerHTML = leaves.slice(0, 10).map(lr => {
        let badge = '<span class="badge badge-pending">PENDING</span>';
        if (lr.status === 'APPROVED') badge = '<span class="badge badge-approved">APPROVED</span>';
        if (lr.status === 'REJECTED') badge = '<span class="badge badge-rejected">REJECTED</span>';

        return `
          <tr>
            <td><strong>${escapeHtml(lr.leaveType)}</strong></td>
            <td>${lr.fromDate}</td>
            <td>${lr.toDate}</td>
            <td style="max-width: 200px;">${escapeHtml(lr.reason)}</td>
            <td>${badge}</td>
          </tr>
        `;
      }).join('');
    }

    openModal('viewProfileModal');
  } catch (err) {
    showToast('Failed to load employee details', 'error');
  }
}

// ================= SECTION 3: ATTENDANCE =================
async function loadAttendance() {
  const search = document.getElementById('attFilterSearch')?.value || '';
  const dept = document.getElementById('attFilterDepartment')?.value || '';
  const date = document.getElementById('attFilterDate')?.value || '';
  const status = document.getElementById('attFilterStatus')?.value || '';

  let query = '/api/admin/attendance?';
  if (search) query += `search=${encodeURIComponent(search)}&`;
  if (dept) query += `department=${encodeURIComponent(dept)}&`;
  if (date) query += `date=${encodeURIComponent(date)}&`;
  if (status) query += `status=${encodeURIComponent(status)}&`;

  try {
    const res = await fetch(query);
    const data = await res.json();
    if (!data.success) return;

    renderAttendanceTable(data.attendance || []);
  } catch (err) {
    console.error('Failed to load attendance:', err);
  }
}

function applyAttendanceFilters() {
  loadAttendance();
}

function resetAttendanceFilters() {
  if (document.getElementById('attFilterSearch')) document.getElementById('attFilterSearch').value = '';
  if (document.getElementById('attFilterDepartment')) document.getElementById('attFilterDepartment').value = '';
  if (document.getElementById('attFilterDate')) document.getElementById('attFilterDate').value = '';
  if (document.getElementById('attFilterStatus')) document.getElementById('attFilterStatus').value = '';
  loadAttendance();
}

function renderAttendanceTable(records) {
  const tbody = document.getElementById('attendanceTableBody');
  if (!tbody) return;

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 20px;">No attendance records found matching filters.</td></tr>';
    return;
  }

  tbody.innerHTML = records.map(att => {
    const punchInStr = att.punchIn ? formatTime(att.punchIn) : '-';
    const punchOutStr = att.punchOut ? formatTime(att.punchOut) : '<span style="color: var(--warning); font-weight: 600;">Present / Working</span>';
    const statusText = att.status || (att.punchOut ? 'PRESENT' : 'IN PROGRESS');

    // Avatar
    let avatarHtml = '';
    if (att.photo) {
      avatarHtml = `<img src="${att.photo}" alt="${escapeHtml(att.employeeName)}" class="table-avatar" onerror="this.outerHTML='<div class=\\'table-avatar\\'>${getInitials(att.employeeName)}</div>'">`;
    } else {
      avatarHtml = `<div class="table-avatar">${getInitials(att.employeeName)}</div>`;
    }

    return `
      <tr>
        <td>${avatarHtml}</td>
        <td>
          <strong>${escapeHtml(att.employeeName)}</strong><br>
          <small style="color: var(--text-muted);">${escapeHtml(att.department || '-')}</small>
        </td>
        <td><span class="badge" style="background:#f1f5f9; color:#475569;">${escapeHtml(att.employeeCode)}</span></td>
        <td>${att.workDate}</td>
        <td><strong>${punchInStr}</strong></td>
        <td>${punchOutStr}</td>
        <td><span class="badge ${att.punchOut ? 'badge-approved' : 'badge-pending'}">${escapeHtml(statusText)}</span></td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="openAttendanceDetailsModal(${att.id})">
            View Details
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

async function openAttendanceDetailsModal(id) {
  try {
    const res = await fetch(`/api/admin/attendance/${id}`);
    const data = await res.json();
    if (!data.success || !data.attendance) {
      showToast('❌ Attendance record not found', 'error');
      return;
    }
    const a = data.attendance;
    document.getElementById('attModalEmpName').textContent = a.employeeName || 'Employee';
    document.getElementById('attModalEmpInfo').textContent = `${a.employeeCode} • ${a.department || 'General'} • Date: ${a.workDate}`;

    // Avatar
    const photoEl = document.getElementById('attModalEmpPhoto');
    if (a.photo) {
      photoEl.innerHTML = `<img src="${a.photo}" alt="${escapeHtml(a.employeeName)}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;">`;
    } else {
      photoEl.innerHTML = getInitials(a.employeeName);
    }

    // Punch In Photo
    const inImg = document.getElementById('attModalPunchInImg');
    const inTimeEl = document.getElementById('attModalPunchInTime');
    if (a.punchInPhoto) {
      inImg.src = a.punchInPhoto;
    } else {
      inImg.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23f1f5f9"/><text x="50%" y="58%" font-size="24" fill="%2394a3b8" text-anchor="middle">No Photo</text></svg>';
    }
    inTimeEl.textContent = a.punchIn ? `Punch In: ${formatTime(a.punchIn)}` : 'Not punched in';

    // Punch Out Photo
    const outImg = document.getElementById('attModalPunchOutImg');
    const outTimeEl = document.getElementById('attModalPunchOutTime');
    if (a.punchOutPhoto) {
      outImg.src = a.punchOutPhoto;
    } else {
      outImg.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23f1f5f9"/><text x="50%" y="58%" font-size="24" fill="%2394a3b8" text-anchor="middle">No Photo</text></svg>';
    }
    outTimeEl.textContent = a.punchOut ? `Punch Out: ${formatTime(a.punchOut)}` : 'Not punched out';

    // Correction inputs
    document.getElementById('attCorrectRecordId').value = a.id;
    document.getElementById('attCorrectPunchIn').value = a.punchIn ? a.punchIn.substring(0, 5) : '';
    document.getElementById('attCorrectPunchOut').value = a.punchOut ? a.punchOut.substring(0, 5) : '';
    document.getElementById('attCorrectStatus').value = a.status || (a.punchOut ? 'PRESENT' : 'IN PROGRESS');

    openModal('attendanceDetailsModal');
  } catch (err) {
    showToast('❌ Failed to fetch attendance details', 'error');
  }
}

async function submitAttendanceCorrection() {
  const id = document.getElementById('attCorrectRecordId').value;
  if (!id) return;

  const payload = {
    punchIn: document.getElementById('attCorrectPunchIn').value || null,
    punchOut: document.getElementById('attCorrectPunchOut').value || null,
    status: document.getElementById('attCorrectStatus').value
  };

  try {
    const res = await fetch(`/api/admin/attendance/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast('✅ Attendance updated successfully!', 'success');
      closeModal('attendanceDetailsModal');
      await loadAttendance();
      await loadDashboardData();
    } else {
      showToast('❌ ' + (data.message || 'Correction failed'), 'error');
    }
  } catch (err) {
    showToast('❌ Network error updating attendance', 'error');
  }
}

let currentViewedEmpId = null;
let currentViewedEmpName = '';
let currentViewedEmpCode = '';

function openResetPasswordFromView() {
  if (currentViewedEmpId) {
    openAdminResetPasswordModal(currentViewedEmpId, currentViewedEmpName, currentViewedEmpCode);
  }
}

let currentResetEmpId = null;
function openAdminResetPasswordModal(empId, empName, empCode) {
  currentResetEmpId = empId;
  document.getElementById('resetPassEmpId').value = empId;
  document.getElementById('resetPassEmpName').textContent = empName;
  document.getElementById('resetPassEmpCode').textContent = empCode;
  document.getElementById('adminNewPass').value = '';
  document.getElementById('adminConfirmPass').value = '';
  openModal('adminResetPasswordModal');
}

async function submitAdminResetPassword(e) {
  e.preventDefault();
  const newPass = document.getElementById('adminNewPass').value;
  const confirmPass = document.getElementById('adminConfirmPass').value;

  if (newPass !== confirmPass) {
    showToast('❌ Passwords do not match.', 'error');
    return;
  }
  if (newPass.length < 4) {
    showToast('❌ Password must be at least 4 characters long.', 'error');
    return;
  }

  try {
    const res = await fetch(`/api/admin/employees/${currentResetEmpId}/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ newPassword: newPass })
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showToast('✅ Password updated successfully for employee!', 'success');
      closeModal('adminResetPasswordModal');
    } else {
      showToast('❌ ' + (data.message || 'Failed to update password'), 'error');
    }
  } catch (err) {
    showToast('❌ Network error resetting password', 'error');
  }
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

// ================= SECTION 4: LEAVE REQUESTS =================
async function loadLeaveRequests() {
  const statusFilter = document.getElementById('leaveStatusFilter')?.value || 'ALL';
  const query = statusFilter !== 'ALL' ? `/api/admin/leaves?status=${statusFilter}` : '/api/admin/leaves';

  try {
    const res = await fetch(query);
    const data = await res.json();
    if (!data.success) return;

    renderLeavesTable(data.leaves || []);
  } catch (err) {
    console.error('Failed to load leave requests:', err);
  }
}

function renderLeavesTable(leaves) {
  const tbody = document.getElementById('leavesTableBody');
  if (!tbody) return;

  if (leaves.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 20px;">No leave applications found.</td></tr>';
    return;
  }

  tbody.innerHTML = leaves.map(lr => {
    let statusBadge = '';
    if (lr.status === 'PENDING') statusBadge = '<span class="badge badge-pending">PENDING</span>';
    else if (lr.status === 'APPROVED') statusBadge = '<span class="badge badge-approved">APPROVED</span>';
    else if (lr.status === 'REJECTED') statusBadge = '<span class="badge badge-rejected">REJECTED</span>';

    let actionContent = '';
    if (lr.status === 'PENDING') {
      actionContent = `<button class="btn btn-primary btn-sm" onclick="openReviewLeaveModal(${lr.id})">Review Request</button>`;
    } else {
      actionContent = lr.adminNote ? `<small><em>Note: ${escapeHtml(lr.adminNote)}</em></small>` : '<small class="text-muted">Reviewed</small>';
    }

    const appliedDateStr = lr.appliedAt ? lr.appliedAt.replace('T', ' ').substring(0, 16) : '-';

    return `
      <tr>
        <td>
          <strong>${escapeHtml(lr.employeeName)}</strong><br>
          <small style="color: var(--text-muted);">${escapeHtml(lr.employeeCode)}</small>
        </td>
        <td>${escapeHtml(lr.department || '-')}</td>
        <td><strong>${escapeHtml(lr.leaveType)}</strong></td>
        <td>${lr.fromDate}</td>
        <td>${lr.toDate}</td>
        <td style="max-width: 220px; white-space: normal;">${escapeHtml(lr.reason)}</td>
        <td>${statusBadge}</td>
        <td>${appliedDateStr}</td>
        <td>${actionContent}</td>
      </tr>
    `;
  }).join('');
}

function openReviewLeaveModal(leaveId) {
  currentReviewLeaveId = leaveId;
  fetch('/api/admin/leaves')
    .then(r => r.json())
    .then(data => {
      const lr = (data.leaves || []).find(l => l.id === leaveId);
      if (lr) {
        document.getElementById('reviewLeaveEmpName').textContent = lr.employeeName;
        document.getElementById('reviewLeaveEmpCode').textContent = lr.employeeCode;
        document.getElementById('reviewLeaveDates').textContent = `${lr.fromDate} to ${lr.toDate}`;
        document.getElementById('reviewLeaveType').textContent = lr.leaveType;
        document.getElementById('reviewLeaveReason').textContent = lr.reason;
        document.getElementById('reviewAdminNote').value = '';
        openModal('reviewLeaveModal');
      }
    });
}

async function submitLeaveDecision(decision) {
  if (!currentReviewLeaveId) return;

  const adminNote = document.getElementById('reviewAdminNote').value.trim();

  try {
    const res = await fetch(`/api/admin/leaves/${currentReviewLeaveId}/review`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ decision, adminNote })
    });
    const data = await res.json();

    if (res.ok && data.success) {
      showToast(`Leave request ${decision.toLowerCase()} successfully!`, 'success');
      closeModal('reviewLeaveModal');
      loadLeaveRequests();
      loadDashboardData();
    } else {
      showToast(data.message || 'Failed to update leave request', 'error');
    }
  } catch (err) {
    showToast('Network error while reviewing leave', 'error');
  }
}

// ================= SECTION 5: REPORTS =================
function renderReports() {
  const activeCountEl = document.getElementById('reportActiveCount');
  const biometricRateEl = document.getElementById('reportBiometricRate');
  const deptListEl = document.getElementById('reportsDeptList');

  if (!activeCountEl || !biometricRateEl || !deptListEl) return;

  const total = allEmployeesCache.length;
  const active = allEmployeesCache.filter(e => e.status === 'ACTIVE').length;
  const enrolled = allEmployeesCache.filter(e => e.faceEnrolled).length;

  activeCountEl.textContent = active;
  biometricRateEl.textContent = total > 0 ? `${Math.round((enrolled / total) * 100)}%` : '0%';

  // Count per department
  const deptCounts = {};
  allEmployeesCache.forEach(e => {
    const d = e.department || 'Unassigned';
    deptCounts[d] = (deptCounts[d] || 0) + 1;
  });

  deptListEl.innerHTML = Object.entries(deptCounts).map(([dept, count]) => {
    const pct = total > 0 ? Math.round((count / total) * 100) : 0;
    return `
      <div style="background: #f8fafc; border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 14px;">
        <div style="display: flex; justify-content: space-between; font-weight: 600; margin-bottom: 6px;">
          <span>${escapeHtml(dept)}</span>
          <span style="color: var(--primary);">${count} staff</span>
        </div>
        <div style="width: 100%; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden;">
          <div style="width: ${pct}%; height: 100%; background: var(--primary);"></div>
        </div>
      </div>
    `;
  }).join('');
}

// Modal Helpers
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
  if (!document.querySelector('.modal-overlay.active')) {
    document.body.classList.remove('modal-open');
  }
}

function togglePasswordVisibility(inputId) {
  const input = document.getElementById(inputId);
  if (input) {
    input.type = input.type === 'password' ? 'text' : 'password';
  }
}

// Close modals when clicking backdrop
document.addEventListener('click', (e) => {
  if (e.target && e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
    if (!document.querySelector('.modal-overlay.active')) {
      document.body.classList.remove('modal-open');
    }
  }
});

// Close modal on ESC key
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    const activeModals = document.querySelectorAll('.modal-overlay.active');
    activeModals.forEach(m => m.classList.remove('active'));
    document.body.classList.remove('modal-open');
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
