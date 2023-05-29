package org.golo.jjtree;

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (with java-like unicode escape processing).
 */

public
class JavaCharStream
{
  /** Whether parser is static. */
  static final boolean staticFlag = false;

  static final int hexval(char c) throws java.io.IOException {
    switch(c)
    {
       case '0' :
          return 0;
       case '1' :
          return 1;
       case '2' :
          return 2;
       case '3' :
          return 3;
       case '4' :
          return 4;
       case '5' :
          return 5;
       case '6' :
          return 6;
       case '7' :
          return 7;
       case '8' :
          return 8;
       case '9' :
          return 9;

       case 'a' :
       case 'A' :
          return 10;
       case 'b' :
       case 'B' :
          return 11;
       case 'c' :
       case 'C' :
          return 12;
       case 'd' :
       case 'D' :
          return 13;
       case 'e' :
       case 'E' :
          return 14;
       case 'f' :
       case 'F' :
          return 15;
    }

    throw new java.io.IOException(); // Should never come here
  }

/* Position in buffer. */
  int bufpos = -1;
  int bufsize;
  int available;
  int tokenBegin;
  int bufline[];
  int bufcolumn[];

  int column = 0;
  int line = 1;

  boolean prevCharIsCR = false;
  boolean prevCharIsLF = false;

  java.io.Reader inputStream;

  char[] nextCharBuf;
  char[] buffer;
  int maxNextCharInd = 0;
  int nextCharInd = -1;
  int inBuf = 0;
  int tabSize = 1;
  boolean trackLineColumn = true;

  void setTabSize(int i) { tabSize = i; }
  int getTabSize() { return tabSize; }

  void ExpandBuff(boolean wrapAround)
  {
    char[] newbuffer = new char[bufsize + 2048];
    int newbufline[] = new int[bufsize + 2048];
    int newbufcolumn[] = new int[bufsize + 2048];

    try
    {
      if (wrapAround)
      {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
        bufcolumn = newbufcolumn;

        bufpos += (bufsize - tokenBegin);
    }
    else
    {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        bufcolumn = newbufcolumn;

        bufpos -= tokenBegin;
      }
    }
    catch (Throwable t)
    {
      throw new Error(t.getMessage());
    }

    available = (bufsize += 2048);
    tokenBegin = 0;
  }

  void FillBuff() throws java.io.IOException
  {
    int i;
    if (maxNextCharInd == 4096)
      maxNextCharInd = nextCharInd = 0;

    try {
      if ((i = inputStream.read(nextCharBuf, maxNextCharInd,
                                          4096 - maxNextCharInd)) == -1)
      {
        inputStream.close();
        throw new java.io.IOException();
      }
      else
         maxNextCharInd += i;
      return;
    }
    catch(java.io.IOException e) {
      if (bufpos != 0)
      {
        --bufpos;
        backup(0);
      }
      else
      {
        bufline[bufpos] = line;
        bufcolumn[bufpos] = column;
      }
      throw e;
    }
  }

  char ReadByte() throws java.io.IOException
  {
    if (++nextCharInd >= maxNextCharInd)
      FillBuff();

    return nextCharBuf[nextCharInd];
  }

/* @return starting character for token. */
  char BeginToken() throws java.io.IOException
  {
    if (inBuf > 0)
    {
      --inBuf;

      if (++bufpos == bufsize)
        bufpos = 0;

      tokenBegin = bufpos;
      return buffer[bufpos];
    }

    tokenBegin = 0;
    bufpos = -1;

    return readChar();
  }

  void AdjustBuffSize()
  {
    if (available == bufsize)
    {
      if (tokenBegin > 2048)
      {
        bufpos = 0;
        available = tokenBegin;
      }
      else
        ExpandBuff(false);
    }
    else if (available > tokenBegin)
      available = bufsize;
    else if ((tokenBegin - available) < 2048)
      ExpandBuff(true);
    else
      available = tokenBegin;
  }

  void UpdateLineColumn(char c)
  {
    column++;

    if (prevCharIsLF)
    {
      prevCharIsLF = false;
      line += (column = 1);
    }
    else if (prevCharIsCR)
    {
      prevCharIsCR = false;
      if (c == '\n')
      {
        prevCharIsLF = true;
      }
      else
        line += (column = 1);
    }

    switch (c)
    {
      case '\r' :
        prevCharIsCR = true;
        break;
      case '\n' :
        prevCharIsLF = true;
        break;
      case '\t' :
        column--;
        column += (tabSize - (column % tabSize));
        break;
      default :
        break;
    }

    bufline[bufpos] = line;
    bufcolumn[bufpos] = column;
  }

/* Read a character. */
  char readChar() throws java.io.IOException
  {
    if (inBuf > 0)
    {
      --inBuf;

      if (++bufpos == bufsize)
        bufpos = 0;

      return buffer[bufpos];
    }

    char c;

    if (++bufpos == available)
      AdjustBuffSize();

    if ((buffer[bufpos] = c = ReadByte()) == '\\')
    {
      if (trackLineColumn) { UpdateLineColumn(c); }

      int backSlashCnt = 1;

      for (;;) // Read all the backslashes
      {
        if (++bufpos == available)
          AdjustBuffSize();

        try
        {
          if ((buffer[bufpos] = c = ReadByte()) != '\\')
          {
            if (trackLineColumn) { UpdateLineColumn(c); }
            // found a non-backslash char.
            if ((c == 'u') && ((backSlashCnt & 1) == 1))
            {
              if (--bufpos < 0)
                bufpos = bufsize - 1;

              break;
            }

            backup(backSlashCnt);
            return '\\';
          }
        }
        catch(java.io.IOException e)
        {
	  // We are returning one backslash so we should only backup (count-1)
          if (backSlashCnt > 1)
            backup(backSlashCnt-1);

          return '\\';
        }

        if (trackLineColumn) { UpdateLineColumn(c); }
        backSlashCnt++;
      }

      // Here, we have seen an odd number of backslash's followed by a 'u'
      try
      {
        while ((c = ReadByte()) == 'u')
          ++column;

        buffer[bufpos] = c = (char)(hexval(c) << 12 |
                                    hexval(ReadByte()) << 8 |
                                    hexval(ReadByte()) << 4 |
                                    hexval(ReadByte()));

        column += 4;
      }
      catch(java.io.IOException e)
      {
        throw new Error("Invalid escape character at line " + line +
                                         " column " + column + ".");
      }

      if (backSlashCnt == 1)
        return c;
      else
      {
        backup(backSlashCnt - 1);
        return '\\';
      }
    }
    else
    {
      UpdateLineColumn(c);
      return c;
    }
  }

  /*
   * @deprecated
   * @see #getEndColumn
   */
  @Deprecated
  int getColumn() {
    return bufcolumn[bufpos];
  }

  /*
   * @deprecated
   * @see #getEndLine
   * @return the line number.
   */
  @Deprecated
  int getLine() {
    return bufline[bufpos];
  }

/** Get end column.
 * @return the end column or -1
 */
  int getEndColumn() {
    return bufcolumn[bufpos];
  }

/** Get end line.
 * @return the end line number or -1
 */
  int getEndLine() {
    return bufline[bufpos];
  }

/** Get the beginning column.
 * @return column of token start */
  int getBeginColumn() {
    return bufcolumn[tokenBegin];
  }

/** @return line number of token start */
  int getBeginLine() {
    return bufline[tokenBegin];
  }

/** Retreat. */
  void backup(int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
      bufpos += bufsize;
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @param buffersize size of the buffer
 */
  JavaCharStream(java.io.Reader dstream,
                 int startline, int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = buffersize;
    buffer = new char[buffersize];
    bufline = new int[buffersize];
    bufcolumn = new int[buffersize];
    nextCharBuf = new char[4096];
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 */
  JavaCharStream(java.io.Reader dstream,
                                        int startline, int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 */
  JavaCharStream(java.io.Reader dstream)
  {
    this(dstream, 1, 1, 4096);
  }
/* Reinitialise. */
  void ReInit(java.io.Reader dstream,
                 int startline, int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    if (buffer == null || buffersize != buffer.length)
    {
      available = bufsize = buffersize;
      buffer = new char[buffersize];
      bufline = new int[buffersize];
      bufcolumn = new int[buffersize];
      nextCharBuf = new char[4096];
    }
    prevCharIsLF = prevCharIsCR = false;
    tokenBegin = inBuf = maxNextCharInd = 0;
    nextCharInd = bufpos = -1;
  }

/* Reinitialise. */
  void ReInit(java.io.Reader dstream,
                                        int startline, int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }

/* Reinitialise. */
  void ReInit(java.io.Reader dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }
/** Constructor. */
  JavaCharStream(java.io.InputStream dstream, String encoding, int startline,
  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    this(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @param buffersize size of the buffer
 */
  JavaCharStream(java.io.InputStream dstream, int startline,
  int startcolumn, int buffersize)
  {
    this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param encoding the character encoding of the data stream.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @throws UnsupportedEncodingException encoding is invalid or unsupported.
 */
  JavaCharStream(java.io.InputStream dstream, String encoding, int startline,
                        int startcolumn) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, startline, startcolumn, 4096);
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 */
  JavaCharStream(java.io.InputStream dstream, int startline,
                        int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

/** Constructor.
 * @param dstream the underlying data source.
 * @param encoding the character encoding of the data stream.
 * @throws UnsupportedEncodingException encoding is invalid or unsupported.
 */
  JavaCharStream(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, 1, 1, 4096);
  }

  /** Constructor.
   * @param dstream the underlying data source.
   */
  JavaCharStream(java.io.InputStream dstream)
  {
    this(dstream, 1, 1, 4096);
  }

/** Reinitialise.
 * @param dstream the underlying data source.
 * @param encoding the character encoding of the data stream.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @param buffersize size of the buffer
 */
  void ReInit(java.io.InputStream dstream, String encoding, int startline,
  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    ReInit(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

/** Reinitialise.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @param buffersize size of the buffer
 */
  void ReInit(java.io.InputStream dstream, int startline,
  int startcolumn, int buffersize)
  {
    ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }
/** Reinitialise.
 * @param dstream the underlying data source.
 * @param encoding the character encoding of the data stream.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 * @throws UnsupportedEncodingException encoding is invalid or unsupported.
 */
  void ReInit(java.io.InputStream dstream, String encoding, int startline,
                     int startcolumn) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, startline, startcolumn, 4096);
  }
/** Reinitialise.
 * @param dstream the underlying data source.
 * @param startline line number of the first character of the stream, mostly for error messages.
 * @param startcolumn column number of the first character of the stream.
 */
  void ReInit(java.io.InputStream dstream, int startline,
                     int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }
/** Reinitialise.
 * @param dstream the underlying data source.
 * @param encoding the character encoding of the data stream.
 * @throws UnsupportedEncodingException encoding is invalid or unsupported.
 */
  void ReInit(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, 1, 1, 4096);
  }

/** Reinitialise.
 * @param dstream the underlying data source.
 */
  void ReInit(java.io.InputStream dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }

  /** Get the token timage.
   * @return token image as String */
  String GetImage()
  {
    if (bufpos >= tokenBegin)
      return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
    else
      return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                              new String(buffer, 0, bufpos + 1);
  }

  /** Get the suffix as an array of characters.
   * @param len the length of the array to return.
   * @return suffix */
  char[] GetSuffix(int len)
  {
    char[] ret = new char[len];

    if ((bufpos + 1) >= len)
      System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
    else
    {
      System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                                                        len - bufpos - 1);
      System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
    }

    return ret;
  }

  /** Set buffers back to null when finished. */
  void Done()
  {
    nextCharBuf = null;
    buffer = null;
    bufline = null;
    bufcolumn = null;
  }

  /**
   * Method to adjust line and column numbers for the start of a token.
   *
   * @param newLine the new line number.
   * @param newCol the new column number.
   */
  void adjustBeginLineColumn(int newLine, int newCol)
  {
    int start = tokenBegin;
    int len;

    if (bufpos >= tokenBegin)
    {
      len = bufpos - tokenBegin + inBuf + 1;
    }
    else
    {
      len = bufsize - tokenBegin + bufpos + 1 + inBuf;
    }

    int i = 0, j = 0, k = 0;
    int nextColDiff = 0, columnDiff = 0;

    while (i < len && bufline[j = start % bufsize] == bufline[k = ++start % bufsize])
    {
      bufline[j] = newLine;
      nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
      bufcolumn[j] = newCol + columnDiff;
      columnDiff = nextColDiff;
      i++;
    }

    if (i < len)
    {
      bufline[j] = newLine++;
      bufcolumn[j] = newCol + columnDiff;

      while (i++ < len)
      {
        if (bufline[j = start % bufsize] != bufline[++start % bufsize])
          bufline[j] = newLine++;
        else
          bufline[j] = newLine;
      }
    }

    line = bufline[j];
    column = bufcolumn[j];
  }
  boolean getTrackLineColumn() { return trackLineColumn; }
  void setTrackLineColumn(boolean tlc) { trackLineColumn = tlc; }

}
