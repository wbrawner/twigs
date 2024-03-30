const forms = document.getElementsByTagName('form')

for (let i = 0; i < forms.length; i++) {
    const form = forms[i]
    form.onsubmit = () => {
        form.querySelector('input[type="submit"]').disabled = true
    }
}