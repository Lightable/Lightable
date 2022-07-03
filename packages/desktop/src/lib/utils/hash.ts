
export default async function hash(str: string) {
    return convertBufferToHex(await digestMessage(str))
} 

async function digestMessage(message: string) {
    const encoder = new TextEncoder();
    const data = encoder.encode(message);
    const hash = await crypto.subtle.digest('SHA-256', data);
    return hash;
  }
  function convertBufferToHex (buffer: ArrayBuffer) {
    var data = new DataView(buffer), i = 0, 
        dataLength = data.byteLength, cData = null,
        hexValue = '';

    while (i < dataLength) {
        cData = data.getUint8(i).toString(16);
        if (cData.length < 2) {
            cData = '0' + cData;
        }

        hexValue += cData;
        i += 1;
    }
    return hexValue;
}
  