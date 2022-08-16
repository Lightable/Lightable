export function parseMarkdown(content: string) {
    if (!content) return '';
    const removeHTML = content.replace(/(<([^>]+)>)/gi, "");
    let parsed = removeHTML
        .replace(/(\*\*)(.*?)\1/gim, '<span class="m-bold">$2</span>')                 // bold
        .replace(/(\_\_)(.*?)\1/gim, '<span class="m-underline">$2</span>')            // underline
        .replace(/(\*|_)(.*?)\1/gim, '<span class="m-italic">$2</span>')               // emphasis
        .replace(/`(.*?)`/gim, '<span class="m-inline-code">$1</span>')                // inline code
        .replace(/(~~)(.*?)\1/gim, '<span class="m-inline">$2</span>')           // line-through
        .replace(/\\n/gm, '<br />')                                                  // new line
        .replace(/\[(.*?)\]\((.*?)\)/gim, `<button class="m-link" onclick="window.popoverURLOpen('$2')">$1</button>`)  // link
        // TODO: Fix generic https url parsing 
        // .replace(/(?:https?):\/\/(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%!\-\/]))?/gim, `<button class="m-link" onclick="window.popoverURLOpen('$1')">$1</button>`)
    return parsed;
}