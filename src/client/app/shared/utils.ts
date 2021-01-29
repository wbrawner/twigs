const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

export function randomId(): string {
    var bytes = new Uint8Array(32)
    window.crypto.getRandomValues(bytes)
    return Array.from(bytes, (byte) => CHARACTERS[byte % CHARACTERS.length]).join('')
}
