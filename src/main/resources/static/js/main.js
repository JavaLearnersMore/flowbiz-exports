/* Shared header/footer + form handling for all pages */
(function () {
  const COMPANY = "Flowbiz Exports Pvt. Ltd.";
  const pages = [
    ["index.html", "Home"],
    ["about.html", "About Us"],
    ["products.html", "Products"],
    ["services.html", "Services & Quality"],
    ["contact.html", "Contact Us"]
  ];
  const current = location.pathname.split("/").pop() || "index.html";

  function headerHtml() {
    const links = pages.map(([href, label]) =>
      `<li><a href="${href}" class="${current === href ? "active" : ""}">${label}</a></li>`).join("");
    return `
    <div class="container nav">
      <a class="brand" href="index.html">
        <img src="images/logo.svg" alt="Flowbiz Exports logo">
        <span>Flowbiz Exports<small>Pvt. Ltd.</small></span>
      </a>
      <nav><ul>${links}<li id="authSlot"></li></ul></nav>
    </div>`;
  }

  function footerHtml() {
    return `
    <div class="container">
      <div class="cols">
        <div><h4>${COMPANY}</h4><p>Industrial valves manufacturing, supply and export.</p></div>
        <div><h4>Quick Links</h4>${pages.map(([h, l]) => `<a href="${h}">${l}</a>`).join("")}</div>
        <div><h4>Contact</h4>
          <a href="mailto:info@flowbizexports.com">info@flowbizexports.com</a>
          <a href="tel:+910000000000">+91 00000 00000</a>
        </div>
      </div>
      <div class="copy">&copy; ${new Date().getFullYear()} ${COMPANY} All rights reserved.</div>
    </div>`;
  }

  const h = document.getElementById("site-header");
  const f = document.getElementById("site-footer");
  if (h) h.innerHTML = headerHtml();
  if (f) f.innerHTML = footerHtml();

  // Login state in nav
  fetch("/api/auth/me").then(r => r.json()).then(d => {
    const slot = document.getElementById("authSlot");
    if (!slot) return;
    if (d.loggedIn) {
      slot.innerHTML = `<a href="#" id="logoutLink">Hi, ${escapeHtml(d.name.split(" ")[0])} (Logout)</a>`;
      document.getElementById("logoutLink").addEventListener("click", e => {
        e.preventDefault();
        fetch("/api/auth/logout", { method: "POST" }).then(() => location.href = "index.html");
      });
    } else {
      slot.innerHTML = `<a href="login.html" class="${current === "login.html" ? "active" : ""}">Login</a>`;
      const reg = document.createElement("li");
      reg.innerHTML = `<a href="register.html" class="btn-nav">Register</a>`;
      slot.after(reg);
    }
  }).catch(() => {
    const slot = document.getElementById("authSlot");
    if (slot) slot.innerHTML = `<a href="login.html">Login</a>`;
  });

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
  }

  // Generic form -> JSON API handler.  <form data-api="/api/..." data-redirect="page.html">
  document.querySelectorAll("form[data-api]").forEach(form => {
    form.addEventListener("submit", async e => {
      e.preventDefault();
      const box = form.querySelector(".msg");
      const btn = form.querySelector("button[type=submit]");
      const body = Object.fromEntries(new FormData(form).entries());
      box.className = "msg"; btn.disabled = true;
      try {
        const res = await fetch(form.dataset.api, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body)
        });
        const data = await res.json().catch(() => ({}));
        if (res.ok) {
          box.className = "msg ok";
          box.textContent = data.message || "Success";
          if (form.dataset.redirect) setTimeout(() => location.href = form.dataset.redirect, 900);
          else form.reset();
        } else {
          box.className = "msg err";
          box.textContent = data.message || "Something went wrong. Please try again.";
        }
      } catch (err) {
        box.className = "msg err";
        box.textContent = "Cannot reach the server. Please try again later.";
      } finally {
        btn.disabled = false;
      }
    });
  });
})();
