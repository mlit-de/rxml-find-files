package de.mlit.rxml.find_files;


import de.mlit.rxml.api.SaxResource;
import de.mlit.rxml.api.helper.AbstractResource;
import de.mlit.rxml.util.AttributeAdapter;
import jregex.util.io.PathPattern;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;
import java.util.List;

/**
 * Created by mlauer on 05/05/15.
 */
public class FindFilesResource extends AbstractResource implements SaxResource {

    protected String dir;
    protected String pattern;

    protected PathPattern[] incudePattern;
    protected PathPattern[] excludePattern;

    protected static PathPattern[] compile(List<String> pattern) {
        PathPattern[] result = new PathPattern[pattern.size()];
        int i = 0;
        for (String s : pattern) {
            result[i++] = new PathPattern(s);
        }
        return result;
    }

    protected static boolean matchesAnyPrefix(PathPattern[] pattern, String string, boolean prefix) {
        for (PathPattern p : pattern) {
            if (prefix ? p.startsWith(string) : p.matches(string)) {
                return true;
            }
        }
        return false;
    }


    public FindFilesResource(String dir, List<String> includes, List<String> excludes) {
        this.dir = dir;
        this.pattern = pattern;
        this.incudePattern = compile(includes);
        this.excludePattern = compile(excludes);
    }

    @Override
    public void runOn(ContentHandler ch) throws SAXException, IOException {
        AttributeAdapter aa = new AttributeAdapter(ch);
        aa.startDocument();
        aa.addAttribute("path", dir);
        aa.startElement("dir");
        File file = new File(dir);
        recurse(aa, file, "./");
        aa.endElement("dir");
        aa.endDocument();
    }

    protected void recurse(AttributeAdapter aa, File file, String path) throws SAXException {
        if (!matchesAnyPrefix(excludePattern, path, false)) {
            boolean isDir = file.isDirectory();
            if(matchesAnyPrefix(incudePattern, path, isDir)) {
                if (isDir) {
                    for (String name : file.list()) {
                        File file2 = new File(file, name);
                        String path2 = path + name + (file2.isDirectory() ? "/" : "");
                        recurse(aa, file2, path2);
                    }
                } else {
                    aa.addAttribute("path", path.substring(2)); // strip "./" from beginning;
                    aa.emptyElement("file");

                }
            }
        }
    }
}
