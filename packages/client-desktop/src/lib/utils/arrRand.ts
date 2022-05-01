export default function(arr: Array<any>) {
    return arr[arr.length * Math.random() | 0];
}