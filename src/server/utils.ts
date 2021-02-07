import { randomInt } from 'crypto';

const CHARACTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

export function randomId(length = 32): string {
    return Array.from(new Array(length), () => { CHARACTERS[randomInt(CHARACTERS.length)] }).join('');
}