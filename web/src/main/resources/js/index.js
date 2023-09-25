window.onload = () => {
    const container = document.getElementsByClassName('center')[0]
    container.innerHTML += `
    <div class="flex-full-width">
        <button class="button-primary" onclick="login()">Login</button>
        <button class="button-secondary" onclick="register()">Register</button>
    </div>`
}

function login() {
    console.log('show login form')
}

function register() {
    console.log('show registration form')
}