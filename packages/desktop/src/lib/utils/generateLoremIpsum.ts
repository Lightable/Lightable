import { LoremIpsum } from "lorem-ipsum";

const lorem = new LoremIpsum({
    sentencesPerParagraph: {
        max: 8,
        min: 4
    },
    wordsPerSentence: {
        max: 16,
        min: 4
    }
});

export function generateParagraph(amount: number) {
    return lorem.generateParagraphs(amount);
}
export function generateSentences(amount: number) {
    return lorem.generateSentences(amount);
}
export function generateWords(amount: number) {
    return lorem.generateWords(amount)
}