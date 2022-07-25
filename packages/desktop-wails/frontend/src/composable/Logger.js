// Color utils (from GEx1)
const fromStr = (str) => str.replace('rgb(', '').replace(')', '').split(', ');
const toStr = ([r, g, b]) => `rgb(${r}, ${g}, ${b})`;

const light = (str, val) => toStr(fromStr(str).map((x) => x * val));


const lightableColor = '252, 91, 86'; 
const regionColor = '101, 104, 247'; 

const makeRegionStyle = (color) => `background-color: rgb(${color}); color: white; border-radius: 4px; border: 2px solid ${light(color, 0.5)}; padding: 3px 6px 3px 6px; font-weight: bold;`;


export const debug = (_region, ...args) => {
  const regions = _region.split('.');

  const regionStrings = regions.map(x => `%c${x}%c`);
  const regionStyling = regions.reduce((res) => 
    res.concat(makeRegionStyle(regionColor), '')
  , []);

  console.log(`%cLightable%c ${regionStrings.join(' ')}`,
    makeRegionStyle(lightableColor),
    '',

    ...regionStyling,
    
    ...args
  );
};