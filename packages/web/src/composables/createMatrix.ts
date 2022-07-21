export function createMatrix(row: number, column: number) {
    let arr: Array<any> = [];

    for (var i = 0; i < row; i++) {
        arr[i] = [];

        for (var j = 0; j < column; j++) {
            arr[i][j] = null;
        }
    }

    return arr;
}