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

async function generate() {
    const extWebsiteResp = await axios.get('https://raw.githubusercontent.com/quarkusio/quarkusio.github.io/develop/_data/extensions.yaml');
    const extLibResp = await axios.get('https://raw.githubusercontent.com/quarkusio/quarkus/master/devtools/common/src/main/filtered/extensions.json');

    const extWebsite = yaml.parse(extWebsiteResp.data);

    const fExtWebsite = extWebsite.categories.map(c => {
        return c.extensions.map(e => ({
            ...e,
            category: c.category,
            categoryId: c['cat-id']
        }));
    }).flat();
    const extLibById = new Map<string, any>(extLibResp.data.map(f => [getId(f), f]));
    const out = fExtWebsite.map((eWebsite, i) => {
        const id = getId(eWebsite);
        const eLib = extLibById.get(id);
        if(!eLib) {
            console.warn('Extension missing in lib ' + id);
            return undefined;
        }
        if(!eWebsite.description) {
            console.warn('Description missing for ' + id);
        }
        if(!eLib.shortName) {
            console.warn('Shortname missing for ' + id);
        }
        return {
            id,
            name: eWebsite.name,
            labels: eWebsite.labels,
            description: eWebsite.description,
            shortName: eLib.shortName,
            category: eWebsite.category,
            order: i,
        }
    }).filter(e => !!e);

    await promises.writeFile('../src/launcher-quarkus/extensions.json', JSON.stringify(out));
}

generate().catch(e => console.error(e));