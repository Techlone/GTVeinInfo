package org.prank.config;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class offers advanced configurations capabilities, allowing to provide
 * various categories for configuration variables.
 */
public class Configuration {
    public static final String ALLOWED_CHARS = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CATEGORY_SPLITTER = ".";
    private static final String CONFIG_VERSION_MARKER = "~CONFIG_VERSION";
    private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\"]+)\"");
    private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\"]+)\"");

    File file;

    private Map<String, ConfigCategory> categories = new TreeMap<>();
    private Map<String, Configuration> children = new TreeMap<>();

    public String defaultEncoding = DEFAULT_ENCODING;
    private String fileName = null;

    public Configuration() {
    }

    /**
     * Create a configuration file for the file given in parameter with the provided config version number.
     */
    public Configuration(File file) {
        this.file = file;
        fileName = file.getAbsolutePath().replace(File.separatorChar, '/').replace("/./", "/");
        try {
            load();
        } catch (Throwable ignored) {
        }
    }


    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    public void load() {
        BufferedReader buffer = null;
        UnicodeInputStreamReader input = null;
        try {
            if (file.getParentFile() != null) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                // Either a previous load attempt failed or the file is new; clear maps
                categories.clear();
                children.clear();
                if (!file.createNewFile())
                    return;
            }

            if (file.canRead()) {
                input = new UnicodeInputStreamReader(new FileInputStream(file), defaultEncoding);
                defaultEncoding = input.getEncoding();
                buffer = new BufferedReader(input);

                String line;
                ConfigCategory currentCat = null;
                ArrayList<String> tmpList = null;
                int lineNum = 0;
                String name = null;

                while (true) {
                    lineNum++;
                    line = buffer.readLine();

                    if (line == null) {
                        if (lineNum == 1)
                            break;
                    }

                    Matcher start = CONFIG_START.matcher(line);
                    Matcher end = CONFIG_END.matcher(line);

                    if (start.matches()) {
                        fileName = start.group(1);
                        categories = new TreeMap<>();
                        continue;
                    } else if (end.matches()) {
                        fileName = end.group(1);
                        Configuration child = new Configuration();
                        child.categories = categories;
                        this.children.put(fileName, child);
                        continue;
                    }

                    int nameStart = -1, nameEnd = -1;
                    boolean skip = false;
                    boolean quoted = false;
                    boolean isFirstNonWhitespaceCharOnLine = true;

                    for (int i = 0; i < line.length() && !skip; ++i) {
                        if (Character.isLetterOrDigit(line.charAt(i)) || ALLOWED_CHARS.indexOf(line.charAt(i)) != -1 || (quoted && line.charAt(i) != '"')) {
                            if (nameStart == -1) {
                                nameStart = i;
                            }

                            nameEnd = i;
                            isFirstNonWhitespaceCharOnLine = false;
                        } else if (!Character.isWhitespace(line.charAt(i))) {
                            switch (line.charAt(i)) {
                                case '#':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    skip = true;
                                    continue;
                                case '"':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    if (quoted) {
                                        quoted = false;
                                    }
                                    if (!quoted && nameStart == -1) {
                                        quoted = true;
                                    }
                                    break;

                                case '{':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    name = line.substring(nameStart, nameEnd + 1);
                                    String qualifiedName = ConfigCategory.getQualifiedName(name, currentCat);

                                    ConfigCategory cat = categories.get(qualifiedName);
                                    if (cat == null) {
                                        currentCat = new ConfigCategory(name, currentCat);
                                        categories.put(qualifiedName, currentCat);
                                    } else {
                                        currentCat = cat;
                                    }
                                    name = null;

                                    break;

                                case '}':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    if (currentCat == null) {
                                        throw new RuntimeException(String.format("Config file corrupt, attepted to close to many categories '%s:%d'", fileName, lineNum));
                                    }
                                    currentCat = currentCat.parent;
                                    break;

                                case '=':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    name = line.substring(nameStart, nameEnd + 1);

                                    if (currentCat == null) {
                                        throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                    }

                                    Property prop = new Property(line.substring(i + 1));
                                    i = line.length();

                                    currentCat.put(name, prop);

                                    break;

                                case ':':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    nameStart = nameEnd = -1;
                                    break;

                                case '<':
                                    if ((tmpList != null && i + 1 == line.length()) || (tmpList == null && i + 1 != line.length())) {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    } else if (i + 1 == line.length()) {
                                        name = line.substring(nameStart, nameEnd + 1);

                                        if (currentCat == null) {
                                            throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                        }

                                        tmpList = new ArrayList<>();

                                        skip = true;
                                    }

                                    break;

                                case '>':
                                    if (tmpList == null) {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    }

                                    if (isFirstNonWhitespaceCharOnLine) {
                                        currentCat.put(name, new Property(tmpList.toArray(new String[tmpList.size()])));
                                        name = null;
                                        tmpList = null;
                                    } // else allow special characters as part of string lists
                                    break;

                                case '~':
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;

                                    if (line.startsWith(CONFIG_VERSION_MARKER)) {
                                        skip = true;
                                    }
                                    break;

                                default:
                                    if (tmpList != null) // allow special characters as part of string lists
                                        break;
                                    throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), fileName, lineNum));
                            }
                            isFirstNonWhitespaceCharOnLine = false;
                        }
                    }

                    if (quoted) {
                        throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", fileName, lineNum));
                    } else if (tmpList != null && !skip) {
                        tmpList.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException ignored) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public ConfigCategory getCategory(String category) {
        ConfigCategory ret = categories.get(category);

        if (ret == null) {
            if (category.contains(CATEGORY_SPLITTER)) {
                String[] hierarchy = category.split("\\" + CATEGORY_SPLITTER);
                ConfigCategory parent = categories.get(hierarchy[0]);

                if (parent == null) {
                    parent = new ConfigCategory(hierarchy[0]);
                    categories.put(parent.getQualifiedName(), parent);
                }

                for (int i = 1; i < hierarchy.length; i++) {
                    String name = ConfigCategory.getQualifiedName(hierarchy[i], parent);
                    ConfigCategory child = categories.get(name);

                    if (child == null) {
                        child = new ConfigCategory(hierarchy[i], parent);
                        categories.put(name, child);
                    }

                    ret = child;
                    parent = child;
                }
            } else {
                ret = new ConfigCategory(category);
                categories.put(category, ret);
            }
        }

        return ret;
    }

    public static class UnicodeInputStreamReader extends Reader {
        private final InputStreamReader input;

        public UnicodeInputStreamReader(InputStream source, String encoding) throws IOException {
            String enc = encoding;
            byte[] data = new byte[4];

            PushbackInputStream pbStream = new PushbackInputStream(source, data.length);
            int read = pbStream.read(data, 0, data.length);
            int size = 0;

            int bom16 = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
            int bom24 = bom16 << 8 | (data[2] & 0xFF);
            int bom32 = bom24 << 8 | (data[3] & 0xFF);

            if (bom24 == 0xEFBBBF) {
                enc = "UTF-8";
                size = 3;
            } else if (bom16 == 0xFEFF) {
                enc = "UTF-16BE";
                size = 2;
            } else if (bom16 == 0xFFFE) {
                enc = "UTF-16LE";
                size = 2;
            } else if (bom32 == 0x0000FEFF) {
                enc = "UTF-32BE";
                size = 4;
            } else if (bom32 == 0xFFFE0000) //This will never happen as it'll be caught by UTF-16LE,
            {                             //but if anyone ever runs across a 32LE file, i'd like to disect it.
                enc = "UTF-32LE";
                size = 4;
            }

            if (size < read) {
                pbStream.unread(data, size, read - size);
            }

            this.input = new InputStreamReader(pbStream, enc);
        }

        public String getEncoding() {
            return input.getEncoding();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return input.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}