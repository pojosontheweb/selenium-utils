

package org.monte.media.imgseq;

import java.util.*;
import java.text.*;


public class OSXCollator extends Collator {
    private Collator collator;


    public OSXCollator() {
        this(Locale.getDefault());
    }

    public OSXCollator(Locale locale) {
            collator = Collator.getInstance(locale);

            if (collator instanceof RuleBasedCollator) {
                String rules = ((RuleBasedCollator) collator).getRules();




                int pos = rules.indexOf(",'-'");
                int primaryRelationPos = rules.indexOf('<');
                if (primaryRelationPos == rules.indexOf("'<'")) {
                    primaryRelationPos = rules.indexOf('<', primaryRelationPos + 2);
                }
                if (pos != -1 && pos < primaryRelationPos) {
                    rules = rules.substring(0, pos)
                    + rules.substring(pos + 4, primaryRelationPos)
                    + "<'-'"
                    + rules.substring(primaryRelationPos);
                }




                pos = rules.indexOf(";' '");
                primaryRelationPos = rules.indexOf('<');
                if (primaryRelationPos == rules.indexOf("'<'")) {
                    primaryRelationPos = rules.indexOf('<', primaryRelationPos + 2);
                }
                if (pos != -1 && pos < primaryRelationPos) {
                    rules = rules.substring(0, pos)
                    + rules.substring(pos + 4, primaryRelationPos)
                    + "<' '"
                    + rules.substring(primaryRelationPos);
                }

                try {
                collator = new RuleBasedCollator(rules);
                } catch (ParseException e) {
                    e.printStackTrace();
                    }
            }
    }

    @Override
    public int compare(String source, String target) {
        return collator.compare(expandNumbers(source), expandNumbers(target));
    }

    @Override
    public CollationKey getCollationKey(String source) {
        return collator.getCollationKey(expandNumbers(source));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OSXCollator) {
            OSXCollator that = (OSXCollator) o;
            return this.collator.equals(that.collator);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return collator.hashCode();
    }

    private String expandNumbers(String s) {
        if (s == null) return null;


        StringBuffer out = new StringBuffer();
        StringBuffer digits = new StringBuffer();

        for (int i=0, n = s.length(); i < n; i++) {
            char ch = s.charAt(i);

            if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            } else {
                if (digits.length() != 0) {
                    if (digits.length() < 10) {
                        out.append("00");
                        out.append(digits.length());
                    } else if (digits.length() < 100) {
                        out.append("0");
                        out.append(digits.length());
                    } else if (digits.length() < 1000) {
                        out.append(digits.length());
                    } else if (digits.length() > 999) {
                        out.append("999");
                    }
                    out.append(digits.toString());
                    digits.delete(0, digits.length());
                }
                out.append(ch);
            }
        }
        if (digits.length() != 0) {
            if (digits.length() < 10) {
                out.append("00");
                out.append(digits.length());
            } else if (digits.length() < 100) {
                out.append("0");
                out.append(digits.length());
            } else if (digits.length() < 1000) {
                out.append(digits.length());
            } else if (digits.length() > 999) {
                out.append("999");
            }
            out.append(digits);
        }

        return out.toString();
    }
}
