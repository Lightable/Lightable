export default class Logger {
    logWarn(location: string, text: string, obj: any) {
      if (!obj) {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #ffcb6b;',
          'color: white;'
        );
      } else {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #ffcb6b;',
          'color: white;',
          obj
        );
      }
    }
    logInfo(location: string, text: string, obj: any) {
      if (!obj) {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #82aaff;',
          'color: white;'
        );
      } else {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #82aaff;',
          'color: white;',
          obj
        );
      }
    }
    logSuccess(location: string, text: string, obj: any) {
      if (!obj) {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #03DAC6;',
          'color: white;'
        );
      } else {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #03DAC6;',
          'color: white;',
          obj
        );
      }
    }
    log(location: string, text: string, obj: any) {
      if (!obj) {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #808080;',
          'color: white;'
        );
      } else {
        console.log(
          `%c[${location}]:%c %c${text}%c`,
          'color: #c792ea;',
          'color: white;',
          'color: #808080;',
          'color: white;',
          obj
        );
      }
    }
  }
  