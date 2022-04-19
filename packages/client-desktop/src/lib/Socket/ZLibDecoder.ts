import ISocketDecoder, { SocketResponse } from "./SocketDecode";
import pako from 'pako';
import Pako from "pako";
export default class ZLibDecoder implements ISocketDecoder {
    async decode(data: ArrayBuffer | string): Promise<SocketResponse> {
        let buffer = new Uint8Array(data as ArrayBuffer);
        let inflated = pako.inflate(buffer) as Uint8Array
        return JSON.parse(decodeURIComponent(escape(String.fromCharCode(...inflated)))) as SocketResponse
    }
}
export function Decodeuint8arr(uint8array: Uint8Array) {
    return new TextDecoder("utf-8").decode(uint8array);
}

// export async function inflateZ(buf: Buffer): Promise<string> {
//     return new Promise((resolve, reject) => {
//         zlib.inflate(buf, async (err, data) => {
//             if (!err) {
//                 resolve(data.toString('utf-8'));
//             } else {
//                 reject(err);
//             }
//         })
//     })
// }