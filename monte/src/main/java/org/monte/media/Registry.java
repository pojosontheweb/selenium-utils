
package org.monte.media;

import java.io.File;
import java.util.ArrayList;
import static org.monte.media.FormatKeys.*;


public abstract class Registry {

    private static Registry instance;

    public static Registry getInstance() {
        if (instance == null) {
            instance = new DefaultRegistry();
            instance.init();
        }
        return instance;
    }


    protected abstract void init();


    public abstract void putCodec(Format inputFormat, Format outputFormat, String codecClass);


    public final String[] getDecoderClasses(Format format) {
        return getCodecClasses(format, null);
    }


    public final String[] getEncoderClasses(Format format) {
        return getCodecClasses(null, format);
    }


    public abstract String[] getCodecClasses(
            Format inputFormat,
            Format outputFormat);


    public final Codec[] getDecoders(Format inputFormat) {
        return getCodecs(inputFormat, null);
    }


    public Codec getDecoder(Format inputFormat) {
        return getCodec(inputFormat, null);
    }


    public final Codec[] getEncoders(Format outputFormat) {
        return getCodecs(null, outputFormat);
    }


    public Codec getEncoder(Format outputFormat) {
        return getCodec(null, outputFormat);
    }


    public Codec[] getCodecs(Format inputFormat, Format outputFormat) {
        String[] clazz = getCodecClasses(inputFormat, outputFormat);
        ArrayList<Codec> codecs = new ArrayList<Codec>(clazz.length);
        for (int i = 0; i < clazz.length; i++) {
            try {
                codecs.add((Codec) Class.forName(clazz[i]).newInstance());
            } catch (Exception ex) {

                System.err.println("Monte Registry. Codec class not found: " + clazz[i]);
                unregisterCodec(clazz[i]);
            }
        }
        return codecs.toArray(new Codec[codecs.size()]);
    }


    public Codec getCodec(Format inputFormat, Format outputFormat) {
        String[] clazz = getCodecClasses(inputFormat, outputFormat);
        for (int i = 0; i < clazz.length; i++) {
            try {
                Codec codec = ((Codec) Class.forName(clazz[i]).newInstance());
                codec.setInputFormat(inputFormat);
                if (outputFormat != null) {
                    codec.setOutputFormat(outputFormat);
                }
                return codec;
            } catch (Exception ex) {

                System.err.println("Monte Registry. Codec class not found: " + clazz[i]);
                unregisterCodec(clazz[i]);
            }
        }
        return null;
    }


    public abstract void putReader(Format fileFormat, String readerClass);


    public abstract void putWriter(Format fileFormat, String writerClass);


    public abstract String[] getReaderClasses(Format fileFormat);


    public abstract String[] getWriterClasses(Format fileFormat);

    public MovieReader getReader(Format fileFormat, File file) {
        String[] clazz = getReaderClasses(fileFormat);
        for (int i = 0; i < clazz.length; i++) {
            try {
                return ((MovieReader) Class.forName(clazz[i]).getConstructor(File.class).newInstance(file));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public MovieWriter getWriter(File file) {
        Format format = getFileFormat(file);
        return format == null ? null : getWriter(format, file);
    }

    public MovieWriter getWriter(Format fileFormat, File file) {
        String[] clazz = getWriterClasses(fileFormat);
        for (int i = 0; i < clazz.length; i++) {
            try {
                return ((MovieWriter) Class.forName(clazz[i]).getConstructor(File.class).newInstance(file));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public MovieReader getReader(File file) {
        Format format = getFileFormat(file);
        return format == null ? null : getReader(format, file);
    }

    public abstract void putFileFormat(String extension, Format format);

    public abstract Format getFileFormat(File file);

    public abstract Format[] getReaderFormats();

    public abstract Format[] getWriterFormats();

    public abstract Format[] getFileFormats();

    public abstract String getExtension(Format ff);


    public ArrayList<Format> suggestOutputFormats(Format inputMediaFormat, Format outputFileFormat) {
        ArrayList<Format> formats = new ArrayList<Format>();
        Format matchFormat = new Format(
                MimeTypeKey, outputFileFormat.get(MimeTypeKey),
                MediaTypeKey, inputMediaFormat.get(MediaTypeKey));
        Codec[] codecs = getEncoders(matchFormat);
        int matchingCount = 0;
        for (Codec c : codecs) {
            for (Format mf : c.getOutputFormats(null)) {
                if (mf.matches(matchFormat)) {
                    if (inputMediaFormat.matchesWithout(mf, MimeTypeKey)) {

                        formats.add(0, mf.append(inputMediaFormat));
                        matchingCount++;
                    } else if (inputMediaFormat.matchesWithout(mf, MimeTypeKey, EncodingKey)) {

                        formats.add(matchingCount, mf.append(inputMediaFormat));
                    } else {

                        formats.add(mf.append(inputMediaFormat));
                    }
                }
            }
        }


        for (int i = formats.size() - 1; i >= 0; i--) {
            Format fi = formats.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Format fj = formats.get(j);
                if (fi.matches(fj)) {
                    formats.remove(i);
                    break;
                }
            }
        }

        return formats;
    }

    public abstract void unregisterCodec(String codecClass);
}
