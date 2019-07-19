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
}

function getId(e) {
    return e.groupId + ':' + e.artifactId;
}

async function generate() {
    const extIOResp = await axios.get('https://raw.githubusercontent.com/quarkusio/quarkusio.github.io/develop/_data/extensions.yaml');
    const extCommonResp = await axios.get('https://raw.githubusercontent.com/quarkusio/quarkus/master/devtools/common/src/main/filtered/extensions.json');

    const extIO = yaml.parse(extIOResp.data);

    const fExtIO = extIO.categories.map(c => {
        return c.extensions.map(e => ({
            ...e,
            category: c.category,
            categoryId: c['cat-id']
        }));
    }).flat();
    const fExtIOById = new Map<string, any>(fExtIO.map(f => [getId(f), f]));
    const out = extCommonResp.data.map(e => {
        const id = getId(e);
        const extIO = fExtIOById.get(id);
        if(!extIO) {
            console.warn('Extension missing in UI ' + id);
            return undefined;
        }
        if(!extIO.description) {
            console.warn('Description missing for ' + id);
        }
        if(!e.shortName) {
            console.warn('Shortname missing for ' + id);
        }
        return {
            id,
            name: extIO.name,
            labels: extIO.labels,
            description: extIO.description,
            shortName: e.shortName,
            category: extIO.category,
        }
    }).filter(e => !!e);

    await promises.writeFile('../src/launcher-quarkus/extensions.json', JSON.stringify(out));
}

generate().catch(e => console.error(e));