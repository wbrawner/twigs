const forms = document.getElementsByTagName('form')

for (let i = 0; i < forms.length; i++) {
    const form = forms[i]
    form.onsubmit = () => {
        form.querySelector('input[type="submit"]').disabled = true
    }
}

const sidebar = document.querySelector('#sidebar')

document.querySelector('#hamburger').onclick = (e) => {
    e.preventDefault()
    sidebar.style.transform = 'translateX(0)'
}

document.addEventListener('click', (e) => {
    const style = window.getComputedStyle(sidebar)
    const matrix = new DOMMatrixReadOnly(style.getPropertyValue("transform"))
    if (matrix.m41 === 0) {
        sidebar.style.transform = 'translateX(-100%)'
    }
})