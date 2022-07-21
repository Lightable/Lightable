import { createMatrix } from '../createMatrix';


/**
 *  Column ⤵
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 * ( ) ( ) ( ) ( ) ( ) ( ) <- Row
 */
export default class Connect4 {
    players: string[]
    board: Array<Array<Checker | null>>
    constructor(...players: string[]) {
        this.players = players;
        this.board = createMatrix(6, 7) as Array<Array<Checker | null>>;
    }

    placeChecker(player: string, pos: IPlayerPos) {
        let local = this.board[pos.y][pos.x];

        if (local || this.determineLowestRow(pos) == undefined) {
            return false
        } else {
            this.board[this.determineLowestRow(pos) as number][pos.x] = { player };
            console.log(this.hasHorizontalWin(this.determineLowestRow(pos) as number, player));
        }
    }


    determineLowestRow(pos: IPlayerPos) {
        let slice = [] as Array<boolean>;
        this.board.forEach(r => slice.push(r[pos.x] != null));
        let highestIndex = (slice.indexOf(true) == -1) ? this.board.length - 1 : slice.indexOf(true) - 1
        if (highestIndex < 0) return
        return highestIndex;
    }

    hasHorizontalWin(y: number, player: string) {
        console.log(y, player);
        let row = this.board[y];
        console.log(row);
        const winArr = [];
        row.forEach(c => {
            if (c!!.player == player) {
                winArr.push(player)
            }
        })
        return (winArr.length >= 4);
    }
    hasDiagonalWin(pos: IPlayerPos) {
        var result = false;

        if (this.board[pos.y][pos.x] != null) {
            let numColumns = 7;
            let numRows = 6;
            // there are four possible directions of a win
            // if the top right contains a possible win
            if (pos.y - 3 > -1 && pos.x + 3 < numColumns) {
                result = this.board[pos.y][pos.x] == this.board[pos.y - 1][pos.x + 1] &&
                    this.board[pos.y][pos.x] == this.board[pos.y - 2][pos.x + 2] &&
                    this.board[pos.y][pos.x] == this.board[pos.y - 3][pos.x + 3];
            }
            // if the bottom right contains possible win
            if (pos.y + 3 < numRows && pos.x + 3 < numColumns) {
                result = this.board[pos.y][pos.x] == this.board[pos.y + 1][pos.x + 1] &&
                    this.board[pos.y][pos.x] == this.board[pos.y + 2][pos.x + 2] &&
                    this.board[pos.y][pos.x] == this.board[pos.y + 3][pos.x + 3];
            }
            // if the bottom left contains possible win
            if (pos.y + 3 < numRows && pos.x - 3 > -1) {
                result = this.board[pos.y][pos.x] == this.board[pos.y + 1][pos.x - 1] &&
                    this.board[pos.y][pos.x] == this.board[pos.y + 2][pos.x - 2] &&
                    this.board[pos.y][pos.x] == this.board[pos.y + 3][pos.x - 3];
            }
            // if the top left contains a possible win
            if (pos.y - 3 > -1 && pos.x - 3 > -1) {
                result = this.board[pos.y][pos.x] == this.board[pos.y - 1][pos.x - 1] &&
                    this.board[pos.y][pos.x] == this.board[pos.y - 2][pos.x - 2] &&
                    this.board[pos.y][pos.x] == this.board[pos.y - 3][pos.x - 3];
            }
        }

        return result;
    }
}

export interface Checker {
    player: string
}
export interface IPlayerPos {
    x: number;
    y: number;
}

