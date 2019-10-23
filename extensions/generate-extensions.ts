import axios from 'axios';
import * as yaml from 'yaml';
import { promises} from 'fs';

interface Extension {
    id: string;
    name: string;
    labels: string[];
    description?: string;
    shortName?: string;
    category: string;
    order: number;
}

function getId(e) {
    return e.groupId + ':' + e.artifactId;
}

if(process.argv.length <= 2 || process.argv[2].match('^\d+\.\d+\.\d+$')) {
    throw new Error('Invalid version');
}

const version = process.argv[2];

function flatten(arr) {
  return arr.reduce(function (flat, toFlatten) {
    return flat.concat(Array.isArray(toFlatten) ? flatten(toFlatten) : toFlatten);
  }, []);
}

async function generate() {
    console.log(`Quarkus version: ${version}`);
    const extWebsiteResp = await axios.get('https://raw.githubusercontent.com/quarkusio/quarkusio.github.io/develop/_data/extensions.yaml');
    const extLibResp = await axios.get(`https://repo1.maven.org/maven2/io/quarkus/quarkus-bom-descriptor-json/${version}/quarkus-bom-descriptor-json-${version}.json`);

    const extWebsite = yaml.parse(extWebsiteResp.data);

    const fExtWebsite = flatten(extWebsite.categories.map(c => {
        return c.extensions.map(e => ({
            ...e,
            category: c.category,
            categoryId: c['cat-id']
        }));
    }));
    const extLibById = new Map<string, any>(extLibResp.data.extensions.map(f => [getId(f), f]));
    const out = fExtWebsite.map((eWebsite, i) => {
        const id = getId(eWebsite);
        const eLib = extLibById.get(id);
        if(!eLib) {
            console.warn('Extension missing in lib ' + id);
            return undefined;
        }
        if(!eLib.description) {
            console.warn('Description missing for ' + id);
        }
        if(!eLib.shortName) {
            console.warn('Shortname missing for ' + id);
        }
        return {
            id,
            name: eLib.name,
            labels: eLib.labels,
            description: eLib.description,
            shortName: eLib.shortName,
            category: eWebsite.category,
            order: i,
        }
    }).filter(e => !!e);

    await promises.writeFile('../src/main/resources/quarkus/extensions.json', JSON.stringify(out));
}

generate().catch(e => console.error(e));