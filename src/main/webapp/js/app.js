// ═══════════════════════════════════════════════════
//  app.js  — clean version, no DB sessions
// ═══════════════════════════════════════════════════

var API = {

    token:   function(){ return localStorage.getItem('token');    },
    role:    function(){ return localStorage.getItem('role');     },
    name:    function(){ return localStorage.getItem('fullName'); },
    staffId: function(){ return localStorage.getItem('staffId');  },

    headers: function(){
        return {
            'Content-Type' : 'application/json',
            'Authorization': 'Bearer ' + (this.token() || '')
        };
    },

    _url: function(path){
        if (path.charAt(0) === '/') path = path.substring(1);
        return window.location.origin + '/' + path;
    },

    get: async function(url){
        var res  = await fetch(this._url(url),
            { method:'GET', headers:this.headers() });
        var json = await res.json();
        if (res.status === 401){ Auth.goLogin(); return null; }
        if (!res.ok) throw new Error(json.error || 'Request failed');
        return json;
    },

    post: async function(url, body){
        var res  = await fetch(this._url(url), {
            method : 'POST',
            headers: this.headers(),
            body   : JSON.stringify(body)
        });
        var json = await res.json();
        if (res.status === 401){ Auth.goLogin(); return null; }
        if (!res.ok) throw new Error(json.error || 'Request failed');
        return json;
    },

    put: async function(url, body){
        var res  = await fetch(this._url(url), {
            method : 'PUT',
            headers: this.headers(),
            body   : JSON.stringify(body)
        });
        var json = await res.json();
        if (res.status === 401){ Auth.goLogin(); return null; }
        if (!res.ok) throw new Error(json.error || 'Request failed');
        return json;
    },

    del: async function(url){
        var res  = await fetch(this._url(url),
            { method:'DELETE', headers:this.headers() });
        var json = await res.json();
        if (res.status === 401){ Auth.goLogin(); return null; }
        if (!res.ok) throw new Error(json.error || 'Request failed');
        return json;
    }
};

// ── Auth ──────────────────────────────────────────
var Auth = {
    isLoggedIn: function(){
        return !!localStorage.getItem('token');
    },
    requireAuth: function(){
        if (!this.isLoggedIn()){
            this.goLogin(); return false;
        }
        return true;
    },
    goLogin: function(){
        localStorage.clear();
        // Avoid redirect loop
        if (window.location.pathname.indexOf('index') === -1){
            window.location.href = 'index.html';
        }
    },
    logout: function(){
        var token = localStorage.getItem('token');
        localStorage.clear();
        if (token){
            fetch(window.location.origin + '/api/auth/logout', {
                method:'POST',
                headers:{
                    'Content-Type':'application/json',
                    'Authorization':'Bearer ' + token
                }
            }).finally(function(){
                window.location.href = 'index.html';
            });
        } else {
            window.location.href = 'index.html';
        }
    }
};

// ── Toast ─────────────────────────────────────────
var Toast = {
    show: function(message, type, duration){
        type     = type     || 'success';
        duration = duration || 4000;

        var box = document.getElementById('_toastBox');
        if (!box){
            box = document.createElement('div');
            box.id = '_toastBox';
            Object.assign(box.style, {
                position:'fixed', top:'1rem', right:'1rem',
                zIndex:'9999', minWidth:'260px'
            });
            document.body.appendChild(box);
        }

        var colors = {
            success:'#198754', danger:'#dc3545',
            warning:'#ffc107', info:'#0dcaf0'
        };
        var textColor = (type === 'warning' || type === 'info')
            ? '#000' : '#fff';
        var id  = 'toast_' + Date.now();
        var div = document.createElement('div');
        div.id  = id;
        Object.assign(div.style, {
            background   : colors[type] || colors.info,
            color        : textColor,
            padding      : '.75rem 1rem',
            borderRadius : '8px',
            marginBottom : '.5rem',
            display      : 'flex',
            alignItems   : 'center',
            justifyContent:'space-between',
            boxShadow    : '0 4px 12px rgba(0,0,0,.2)',
            fontSize     : '.875rem',
            animation    : 'fadeIn .3s ease'
        });
        div.innerHTML =
            '<span>' + message + '</span>' +
            '<button onclick="document.getElementById(\'' + id +
            '\').remove()" style="background:transparent;border:none;' +
            'color:' + textColor + ';font-size:1.1rem;cursor:pointer;' +
            'margin-left:.75rem;line-height:1">×</button>';

        box.appendChild(div);
        setTimeout(function(){
            var el = document.getElementById(id);
            if (el) el.remove();
        }, duration);
    }
};

// ── Helpers ───────────────────────────────────────
function fmtDate(d){
    if (!d) return '—';
    try {
        return new Date(d).toLocaleDateString('en-US',
            { year:'numeric', month:'short', day:'numeric' });
    } catch(e){ return d; }
}

function fmtMoney(n){
    return '$' + parseFloat(n || 0).toFixed(2);
}

function statusBadge(status){
    var map = {
        'CONFIRMED'   : 'primary',
        'CHECKED_IN'  : 'success',
        'CHECKED_OUT' : 'secondary',
        'CANCELLED'   : 'danger',
        'AVAILABLE'   : 'success',
        'OCCUPIED'    : 'warning',
        'MAINTENANCE' : 'danger',
        'PAID'        : 'success',
        'PARTIAL'     : 'info',
        'PENDING'     : 'warning',
        'admin'       : 'dark',
        'receptionist': 'primary'
    };
    var c = map[status] || 'secondary';
    var textDark = (c==='warning'||c==='info') ? ' text-dark' : '';
    return '<span class="badge bg-' + c + textDark +
        ' rounded-pill">' +
        (status||'').replace(/_/g,' ') + '</span>';
}

function emptyState(icon, msg){
    return '<div class="text-center py-5 text-muted">' +
        '<i class="bi ' + icon + '" style="font-size:3rem;opacity:.25"></i>' +
        '<p class="mt-2 mb-0">' + msg + '</p></div>';
}

// ── Sidebar ───────────────────────────────────────
function buildSidebar(active){
    var role     = Auth.role() || 'receptionist';
    var name     = Auth.name() || 'User';
    var initials = name.trim().split(' ')
        .map(function(w){ return w[0]||''; })
        .join('').toUpperCase().slice(0,2);

    var links = [
        { p:'dashboard.html',    i:'bi-speedometer2',   l:'Dashboard'   },
        { p:'reservations.html', i:'bi-calendar-check', l:'Reservations'},
        { p:'rooms.html',        i:'bi-door-open',       l:'Rooms'       },
        { p:'guests.html',       i:'bi-people',          l:'Guests'      },
        { p:'billing.html',      i:'bi-receipt',         l:'Billing'     },
        { p:'reports.html',      i:'bi-bar-chart-line',  l:'Reports'     },
        { p:'staff.html',        i:'bi-person-badge',    l:'Staff',
            admin:true }
    ];

    var nav = links
        .filter(function(x){ return !x.admin || role==='admin'; })
        .map(function(x){
            var act = active === x.p ? 'active' : '';
            return '<a href="' + x.p + '" class="nav-link ' + act + '">' +
                '<i class="bi ' + x.i + '"></i> ' + x.l +
                '</a>';
        }).join('');

    return (
        '<div style="padding:1.25rem 1rem;border-bottom:1px solid' +
        ' rgba(255,255,255,.1);text-align:center">' +
        '<div style="width:52px;height:52px;background:#e8a020;' +
        'border-radius:50%;display:flex;align-items:center;' +
        'justify-content:center;margin:0 auto .5rem;' +
        'font-size:1.5rem;color:#fff">' +
        '<i class="bi bi-building"></i></div>' +
        '<div style="color:#fff;font-weight:700;font-size:.9rem">' +
        'Hotel System</div>' +
        '<small style="color:rgba(255,255,255,.5);font-size:.72rem">' +
        'Management System</small>' +
        '</div>' +
        '<div style="padding:.4rem .75rem;font-size:.68rem;font-weight:700;' +
        'color:rgba(255,255,255,.35);text-transform:uppercase;' +
        'letter-spacing:1px;margin-top:.4rem">Navigation</div>' +
        '<nav class="nav flex-column px-2">' + nav + '</nav>' +
        '<div style="margin-top:auto;padding:1rem;border-top:1px solid' +
        ' rgba(255,255,255,.1)">' +
        '<div style="display:flex;align-items:center;gap:.65rem;' +
        'margin-bottom:.65rem">' +
        '<div style="width:36px;height:36px;background:#e8a020;' +
        'border-radius:50%;display:flex;align-items:center;' +
        'justify-content:center;font-weight:700;color:#fff;' +
        'font-size:.82rem;flex-shrink:0">' + initials + '</div>' +
        '<div>' +
        '<div style="color:#fff;font-weight:600;font-size:.85rem">' +
        name + '</div>' +
        '<div style="color:rgba(255,255,255,.5);font-size:.72rem">' +
        role + '</div>' +
        '</div>' +
        '</div>' +
        '<button type="button" onclick="Auth.logout()" ' +
        'class="btn btn-sm btn-outline-light w-100">' +
        '<i class="bi bi-box-arrow-right me-1"></i>Logout</button>' +
        '</div>'
    );
}

// ── Init page ─────────────────────────────────────
function initPage(active){
    if (!Auth.requireAuth()) return;
    var sb = document.getElementById('sidebar');
    if (sb) sb.innerHTML = buildSidebar(active);
    var tu = document.getElementById('topbarUser');
    if (tu) tu.textContent = Auth.name() || '';
    var tr = document.getElementById('topbarRole');
    if (tr) tr.textContent = Auth.role() || '';
    if (Auth.role() !== 'admin'){
        document.querySelectorAll('.admin-only')
            .forEach(function(e){ e.style.display='none'; });
    }
}