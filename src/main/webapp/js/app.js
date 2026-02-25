'use strict';

// ══════════════════════════════════════════════════════════════
//  AUTH — thin wrapper around sessionStorage
// ══════════════════════════════════════════════════════════════
var Auth = {
    get: function(key){ return sessionStorage.getItem(key); },
    set: function(key, val){ sessionStorage.setItem(key, val); },
    clear: function(){ sessionStorage.clear(); },
    name: function(){ return sessionStorage.getItem('userName') || sessionStorage.getItem('name') || 'User'; },
    role: function(){ return (sessionStorage.getItem('userRole') || sessionStorage.getItem('role') || '').toLowerCase(); },
    isAdmin: function(){ return Auth.role() === 'admin'; },
    isLoggedIn: function(){
        return !!(sessionStorage.getItem('authToken') ||
            sessionStorage.getItem('token') ||
            sessionStorage.getItem('sessionId') ||
            sessionStorage.getItem('userId'));
    }
};

// ══════════════════════════════════════════════════════════════
//  API — centralised fetch wrapper
// ══════════════════════════════════════════════════════════════
var API = (function(){
    var BASE = '';   // Tomcat serves from root context

    function getToken(){
        return sessionStorage.getItem('authToken') ||
            sessionStorage.getItem('token') || '';
    }

    function headers(extra){
        var h = { 'Content-Type': 'application/json' };
        var t = getToken();
        if (t) h['Authorization'] = 'Bearer ' + t;
        return Object.assign(h, extra || {});
    }

    async function req(method, url, body){
        var opts = { method: method, headers: headers() };
        if (body !== undefined) opts.body = JSON.stringify(body);
        var res = await fetch(url, opts);
        if (res.status === 401){
            Auth.clear();
            window.location.href = 'index.html';
            return null;
        }
        var text = await res.text();
        if (!text) return {};
        var json;
        try { json = JSON.parse(text); } catch(e){ json = { message: text }; }
        if (!res.ok){
            throw new Error(json.message || json.error || ('HTTP ' + res.status));
        }
        return json;
    }

    return {
        get:    function(url)       { return req('GET',    url); },
        post:   function(url, body) { return req('POST',   url, body); },
        put:    function(url, body) { return req('PUT',    url, body); },
        delete: function(url)       { return req('DELETE', url); }
    };
})();

// ══════════════════════════════════════════════════════════════
//  TOAST notifications
// ══════════════════════════════════════════════════════════════
var Toast = (function(){
    var container;

    function ensure(){
        if (!container){
            container = document.createElement('div');
            container.style.cssText =
                'position:fixed;top:1rem;right:1rem;z-index:9999;' +
                'display:flex;flex-direction:column;gap:.5rem;min-width:280px;max-width:380px';
            document.body.appendChild(container);
        }
    }

    function show(msg, type, ms){
        ensure();
        ms = ms || 3500;
        type = type || 'info';

        var colors = {
            success: { bg:'#d1fae5', border:'#6ee7b7', text:'#065f46', icon:'bi-check-circle-fill' },
            error:   { bg:'#fee2e2', border:'#fca5a5', text:'#991b1b', icon:'bi-exclamation-circle-fill' },
            warning: { bg:'#fef3c7', border:'#fcd34d', text:'#92400e', icon:'bi-exclamation-triangle-fill' },
            info:    { bg:'#dbeafe', border:'#93c5fd', text:'#1e40af', icon:'bi-info-circle-fill' }
        };
        var c = colors[type] || colors.info;

        var el = document.createElement('div');
        el.style.cssText =
            'background:' + c.bg + ';border:1.5px solid ' + c.border + ';' +
            'color:' + c.text + ';border-radius:10px;padding:.75rem 1rem;' +
            'display:flex;align-items:flex-start;gap:.5rem;' +
            'box-shadow:0 4px 16px rgba(0,0,0,.12);' +
            'animation:toastIn .25s ease;font-size:.875rem;';
        el.innerHTML =
            '<i class="bi ' + c.icon + '" style="flex-shrink:0;font-size:1rem;margin-top:.1rem"></i>' +
            '<span style="flex:1">' + msg + '</span>' +
            '<button onclick="this.parentElement.remove()" ' +
            'style="background:none;border:none;color:' + c.text + ';' +
            'cursor:pointer;padding:0;font-size:1rem;opacity:.6;line-height:1">' +
            '&times;</button>';

        container.appendChild(el);
        setTimeout(function(){
            el.style.animation = 'toastOut .25s ease forwards';
            setTimeout(function(){ if (el.parentNode) el.remove(); }, 250);
        }, ms);
    }

    // Inject keyframes once
    var style = document.createElement('style');
    style.textContent =
        '@keyframes toastIn{from{opacity:0;transform:translateX(100%)}to{opacity:1;transform:translateX(0)}}' +
        '@keyframes toastOut{from{opacity:1;transform:translateX(0)}to{opacity:0;transform:translateX(100%)}}';
    document.head.appendChild(style);

    return { show: show };
})();

// ══════════════════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════════════════
function fmtMoney(val){
    var n = parseFloat(val) || 0;
    return '$' + n.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function fmtDate(d){
    if (!d) return '—';
    try {
        return new Date(d).toLocaleDateString('en-US', {
            month:'short', day:'numeric', year:'numeric'
        });
    } catch(e){ return d; }
}

function statusBadge(status){
    var map = {
        'CONFIRMED'   :['#dbeafe','#1e40af','Confirmed'],
        'CHECKED_IN'  :['#d1fae5','#065f46','Checked In'],
        'CHECKED_OUT' :['#f3f4f6','#374151','Checked Out'],
        'CANCELLED'   :['#fee2e2','#991b1b','Cancelled'],
        'PENDING'     :['#fef3c7','#92400e','Pending']
    };
    var s = map[status] || ['#f3f4f6','#374151', status||'Unknown'];
    return '<span style="display:inline-block;padding:.2rem .65rem;' +
        'border-radius:20px;background:' + s[0] + ';color:' + s[1] + ';' +
        'font-size:.75rem;font-weight:700;letter-spacing:.3px">' + s[2] + '</span>';
}

// ══════════════════════════════════════════════════════════════
//  SIDEBAR + PAGE INIT
// ══════════════════════════════════════════════════════════════
function initPage(currentPage){
    // Auth guard
    if (!Auth.isLoggedIn() && currentPage !== 'index.html'){
        window.location.href = 'index.html';
        return;
    }

    // Topbar user info
    var u = document.getElementById('topbarUser');
    var r = document.getElementById('topbarRole');
    if (u) u.textContent = Auth.name();
    if (r){ r.textContent = Auth.role(); r.className = 'badge ' + (Auth.isAdmin() ? 'bg-danger' : 'bg-secondary'); }

    buildSidebar(currentPage);
}

function buildSidebar(currentPage){
    var el = document.getElementById('sidebar');
    if (!el) return;

    var role  = Auth.role();
    var isAdm = Auth.isAdmin();

    // Main nav items
    var navItems = [
        { href:'dashboard.html',    icon:'bi-speedometer2',   label:'Dashboard' },
        { href:'reservations.html', icon:'bi-calendar-check', label:'Reservations' },
        { href:'guests.html',       icon:'bi-people',         label:'Guests' },
        { href:'rooms.html',        icon:'bi-door-open',      label:'Rooms' },
        { href:'billing.html',      icon:'bi-receipt',        label:'Billing' },
        { href:'reports.html',      icon:'bi-bar-chart-line', label:'Reports' },
        { href:'staff.html',        icon:'bi-person-badge',   label:'Staff', adminOnly:true }
    ];

    // Quick action items (same destinations, shorter labels, action-oriented)
    var quickItems = [
        { href:'reservations.html', icon:'bi-plus-circle',    label:'New Reservation' },
        { href:'guests.html',       icon:'bi-person-plus',    label:'Add Guest' },
        { href:'rooms.html',        icon:'bi-search',         label:'Check Availability' },
        { href:'billing.html',      icon:'bi-lightning',      label:'Generate Bill' },
        { href:'reports.html',      icon:'bi-file-earmark-bar-graph', label:'View Reports' },
        { href:'staff.html',        icon:'bi-person-gear',    label:'Manage Staff', adminOnly:true }
    ];

    var navHtml = navItems
        .filter(function(n){ return !n.adminOnly || isAdm; })
        .map(function(n){
            var active = currentPage === n.href ? 'active' : '';
            return '<a href="' + n.href + '" class="nav-link ' + active + '">' +
                '<i class="bi ' + n.icon + '"></i>' + n.label + '</a>';
        }).join('');

    var quickHtml = quickItems
        .filter(function(n){ return !n.adminOnly || isAdm; })
        .map(function(n){
            return '<a href="' + n.href + '" class="nav-link" ' +
                'style="padding:.45rem 1rem .45rem 1.5rem;font-size:.8rem;color:rgba(255,255,255,.6)">' +
                '<i class="bi ' + n.icon + '" style="font-size:.85rem"></i>' + n.label + '</a>';
        }).join('');

    el.innerHTML =
        // Brand
        '<div style="padding:1.25rem 1rem;border-bottom:1px solid rgba(255,255,255,.1)">' +
        '<div style="display:flex;align-items:center;gap:.6rem">' +
        '<div style="width:36px;height:36px;background:rgba(255,255,255,.18);' +
        'border-radius:10px;display:flex;align-items:center;' +
        'justify-content:center;font-size:1.1rem">' +
        '<i class="bi bi-building" style="color:#fff"></i>' +
        '</div>' +
        '<div>' +
        '<div style="color:#fff;font-weight:700;font-size:.9rem;line-height:1.2">Hotel System</div>' +
        '<div style="color:rgba(255,255,255,.5);font-size:.7rem">' + role + '</div>' +
        '</div>' +
        '</div>' +
        '</div>' +

        // Main nav
        '<div style="flex:1;overflow-y:auto;padding:.5rem 0">' +
        '<div style="padding:.5rem 1rem .25rem;font-size:.65rem;' +
        'text-transform:uppercase;letter-spacing:.8px;' +
        'color:rgba(255,255,255,.35);font-weight:700">Navigation</div>' +
        navHtml +

        // ── QUICK ACTIONS ──────────────────────────────────────
        '<div style="margin:.75rem 0 .25rem;padding:.5rem 1rem .25rem;' +
        'font-size:.65rem;text-transform:uppercase;letter-spacing:.8px;' +
        'color:rgba(255,255,255,.35);font-weight:700;' +
        'border-top:1px solid rgba(255,255,255,.08);padding-top:.75rem">' +
        '<i class="bi bi-lightning-fill" style="margin-right:.3rem"></i>Quick Actions</div>' +
        quickHtml +
        // ──────────────────────────────────────────────────────
        '</div>' +

        // Logout at bottom
        '<div style="padding:.75rem;border-top:1px solid rgba(255,255,255,.1)">' +
        '<div style="padding:.4rem 1rem;font-size:.75rem;' +
        'color:rgba(255,255,255,.4);margin-bottom:.4rem">' +
        '<i class="bi bi-person-circle me-1"></i>' + Auth.name() +
        '</div>' +
        '<button onclick="logout()" style="width:100%;padding:.6rem;' +
        'background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.15);' +
        'color:rgba(255,255,255,.8);border-radius:8px;' +
        'font-size:.82rem;cursor:pointer;transition:all .2s;' +
        'display:flex;align-items:center;justify-content:center;gap:.4rem"' +
        ' onmouseover="this.style.background=\'rgba(255,255,255,.18)\'"' +
        ' onmouseout="this.style.background=\'rgba(255,255,255,.08)\'">' +
        '<i class="bi bi-box-arrow-left"></i>Logout' +
        '</button>' +
        '</div>';
}

async function logout(){
    try { await API.post('api/auth/logout', {}); } catch(e){}
    Auth.clear();
    window.location.href = 'index.html';
}