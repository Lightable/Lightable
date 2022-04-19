import formatHighlight from 'json-format-highlight';
export default function(content: string) {
    let customColorOptions = {
        keyColor: 'var(--ac)',
        numberColor: 'var(--blue)',
        stringColor: 'var(--purple)',
        trueColor: 'var(--green)',
        falseColor: 'var(--error)',
        nullColor: 'var(--gray)'
    };
    return formatHighlight(content, customColorOptions);
}