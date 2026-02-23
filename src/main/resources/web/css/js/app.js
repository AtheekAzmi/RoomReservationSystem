// ═══════════════════════════════════════════════════════════════
//  app.js — Shared utility used by ALL pages
// ═══════════════════════════════════════════════════════════════

// ── API Helper ────────────────────────────────────────────────
const API = {

    baseUrl: '',

    token: () => localStorage.getItem('token'),
    role:  () => localStorage.getItem('role'),
    name:  () => localStorage.getItem('fullName'),

    headers() {
        return {
            'Content-Type':  'application/json',
            'Authorization': 'Bearer ' + (this.token() || '')
        };
    },

    async get(url) {
        const res = await fetch(this.baseUrl + url, {
            method:  'GET',
            headers: this.headers()
        });
        if (res.status === 401) { Auth.logout(); return null; }
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Request failed');
        return data;
    },

    async post(url, body) {
        const res = await fetch(this.baseUrl + url, {
            method:  'POST',
            headers: this.headers(),
            body:    JSON.stringify(body)
        });
        if (res.status === 401) { Auth.logout(); return null; }
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Request failed');
        return data;
    },

    async put(url, body) {
        const res = await fetch(this.baseUrl + url, {
            method:  'PUT',
            headers: this.headers(),
            body:    JSON.stringify(body)
        });
        if (res.status === 401) { Auth.logout(); return null; }
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Request failed');
        return data;
    },

    async del(url) {
        const res = await fetch(this.baseUrl + url, {
            method:  'DELETE',
            headers: this.headers()
        });
        if (res.status === 401) { Auth.logout(); return null; }
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Request failed');
        return data;
    }
};

// ── Auth Helper ───────────────────────────────────────────────
const Auth = {
    isLoggedIn() {
        return !!localStorage.getItem('token');
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/index.html';
            return false;
        }
        return true;
    },

    requireAdmin() {
        if (localStorage.getItem('role') !== 'admin') {
            Toast.show('Admin access required', 'danger');
            setTimeout(() => window.location.href = '/dashboard.html', 1500);
            return false;
        }
        return true;
    },

    logout() {
        localStorage.clear();
        window.location.href = '/index.html';
    }
};

// ── Toast Notifications ───────────────────────────────────────
const Toast = {
    show(message, type = 'success', duration = 3500) {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const icons = {
            success: 'bi-check-circle-fill',
            danger:  'bi-exclamation-triangle-fill',
            warning: 'bi-exclamation-circle-fill',
            info:    'bi-info-circle-fill'
        };

        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-bg-${type} border-0 show mb-2`;
        toast.setAttribute('role', 'alert');
        toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body d-flex align-items-center gap-2">
          <i class="bi ${icons[type] || icons.info}"></i>
          <span>${message}</span>
        </div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto"
                onclick="this.closest('.toast').remove()"></button>
      </div>`;

        container.appendChild(toast);
        setTimeout(() => toast.remove(), duration);
    }
};

// ── Loading State ─────────────────────────────────────────────
const Loading = {
    show(containerId, message = 'Loading...') {
        const el = document.getElementById(containerId);
        if (el) el.innerHTML = `
      <div class="loading-overlay">
        <div class="spinner-border spinner-border-sm text-primary"></div>
        <span>${message}</span>
      </div>`;
    },

    hide(containerId, html = '') {
        const el = document.getElementById(containerId);
        if (el) el.innerHTML = html;
    }
};

// ── Status Badge Helper ───────────────────────────────────────
function statusBadge(status) {
    const map = {
        'CONFIRMED':    'badge-confirmed',
        'CHECKED_IN':   'badge-checked-in',
        'CHECKED_OUT':  'badge-checked-out',
        'CANCELLED':    'badge-cancelled',
        'AVAILABLE':    'badge-available',
        'OCCUPIED':     'badge-occupied',
        'MAINTENANCE':  'badge-maintenance',
        'PENDING':      'badge-pending',
        'PAID':         'badge-paid',
        'PARTIAL':      'badge-partial',
        'admin':        'badge-admin',
        'receptionist': 'badge-receptionist'
    };
    const cls = map[status] || 'badge-confirmed';
    return `<span class="status-badge ${cls}">${status.replace('_',' ')}</span>`;
}

// ── Date Formatter ────────────────────────────────────────────
function fmtDate(dateStr) {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
        year: 'numeric', month: 'short', day: 'numeric'
    });
}

// ── Currency Formatter ────────────────────────────────────────
function fmtMoney(amount) {
    return '$' + parseFloat(amount || 0).toFixed(2);
}

// ── Empty State HTML ──────────────────────────────────────────
function emptyState(icon, message) {
    return `
    <div class="empty-state">
      <i class="bi ${icon}"></i>
      <p>${message}</p>
    </div>`;
}

// ── Build Sidebar ─────────────────────────────────────────────
function buildSidebar(activePage) {
    const role     = Auth.role();
    const name     = Auth.name() || 'User';
    const initials = name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0,2);

    const links = [
        { href: 'dashboard.html',    icon: 'bi-speedometer2',  label: 'Dashboard',    roles: ['admin','receptionist'] },
        { href: 'reservations.html', icon: 'bi-calendar-check',label: 'Reservations', roles: ['admin','receptionist'] },
        { href: 'rooms.html',        icon: 'bi-door-open',      label: 'Rooms',        roles: ['admin','receptionist'] },
        { href: 'guests.html',       icon: 'bi-people',         label: 'Guests',       roles: ['admin','receptionist'] },
        { href: 'billing.html',      icon: 'bi-receipt',        label: 'Billing',      roles: ['admin','receptionist'] },
        { href: 'reports.html',      icon: 'bi-bar-chart-line', label: 'Reports',      roles: ['admin','receptionist'] },
        { href: 'staff.html',        icon: 'bi-person-badge',   label: 'Staff',        roles: ['admin'] }
    ];

    const navLinks = links
        .filter(l => l.roles.includes(role))
        .map(l => `
      <a href="${l.href}"
         class="nav-link ${activePage === l.href ? 'active' : ''}">
        <i class="bi ${l.icon}"></i> ${l.label}
      </a>`).join('');

    return `
    <div class="sidebar-header">
      <div class="hotel-icon"><i class="bi bi-building"></i></div>
      <h6>Hotel Reservations</h6>
      <small>Management System</small>
    </div>
    <div class="nav-section-title">Navigation</div>
    <nav class="nav flex-column px-2">
      ${navLinks}
    </nav>
    <div class="sidebar-footer mt-auto">
      <div class="user-info">
        <div class="user-avatar">${initials}</div>
        <div>
          <div style="font-weight:600;color:#fff">${name}</div>
          <div style="font-size:0.75rem">${role}</div>
        </div>
      </div>
      <button onclick="Auth.logout()"
              class="btn btn-sm btn-outline-light w-100 mt-2">
        <i class="bi bi-box-arrow-right"></i> Logout
      </button>
    </div>`;
}

// ── Inject Sidebar ────────────────────────────────────────────
function initPage(activePage) {
    if (!Auth.requireAuth()) return;

    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.innerHTML = buildSidebar(activePage);

    // Show user name in topbar
    const topbarUser = document.getElementById('topbarUser');
    if (topbarUser) topbarUser.textContent = Auth.name() || 'User';

    // Hide admin-only elements for receptionists
    if (Auth.role() !== 'admin') {
        document.querySelectorAll('.admin-only')
            .forEach(el => el.style.display = 'none');
    }
}