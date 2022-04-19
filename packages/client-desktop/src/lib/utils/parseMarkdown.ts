export default function(content: string) {
    if (!content) return '';
    const removeHTML = content.replace(/(<([^>]+)>)/gi, "");
    let parsed = removeHTML
        .replace(/(\*\*)(.*?)\1/gim, '<span class="bold">$2</span>')                 // bold
        .replace(/(\_\_)(.*?)\1/gim, '<span class="underline">$2</span>')            // underline
        .replace(/(\*|_)(.*?)\1/gim, '<span class="italic">$2</span>')               // emphasis
        .replace(/`(.*?)`/gim, '<span class="inline-code">$1</span>')                // inline code
        .replace(/(~~)(.*?)\1/gim, '<span class="line-through">$2</span>')           // line-through
        .replace(/\\n/gm, '<br />')                                                  // new line
        .replace(/\[(.*?)\]\((.*?)\)/gim, "<a class='main-link' href='$2'>$1</a>")  // link
    return parsed;
}