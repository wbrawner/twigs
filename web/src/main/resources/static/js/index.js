const forms = document.getElementsByTagName('form')

for (let i = 0; i < forms.length; i++) {
    const form = forms[i]
    form.onsubmit = () => {
        const inputs = form.getElementsByTagName('input')
        for (let j = 0; j < inputs.length; j++) {
            inputs[j].disabled = true
        }
    }
}