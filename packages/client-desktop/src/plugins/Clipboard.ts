
import { App, Plugin } from "vue";

export const ClipboardPlugin: Plugin = {
    install: (app: App) => {
        app.config.globalProperties.$copyText = (text: string) => {
            if (!navigator.clipboard) {
                const textArea = document.createElement('textarea')
                textArea.value = text

                textArea.style.top = '0'
                textArea.style.left = '0'
                textArea.style.position = 'fixed'

                document.body.appendChild(textArea)
                textArea.focus()
                textArea.select()

                try {
                    document.execCommand('copy')
                    document.body.removeChild(textArea)
                    return Promise.resolve()
                } catch (err) {
                    document.body.removeChild(textArea)
                    return Promise.reject(err)
                }
            }

            return navigator.clipboard.writeText(text)
        }
    }
}
