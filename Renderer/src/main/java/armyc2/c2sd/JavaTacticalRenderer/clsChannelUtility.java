/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c2sd.JavaTacticalRenderer;

import java.util.ArrayList;
import armyc2.c2sd.JavaLineArray.ref;
import armyc2.c2sd.JavaLineArray.POINT2;
import armyc2.c2sd.JavaLineArray.TacticalLines;
import armyc2.c2sd.JavaLineArray.CELineArray;
import armyc2.c2sd.JavaLineArray.Shape2;
import armyc2.c2sd.renderer.utilities.ErrorLogger;
import armyc2.c2sd.renderer.utilities.RendererException;
import armyc2.c2sd.JavaLineArray.Channels;
import armyc2.c2sd.JavaLineArray.lineutility;

/**
 * A class to process channel types.
 *
 * @author Michael Deutch
 */
public final class clsChannelUtility {

    private static final String _className = "clsChannelUtility";

    /**
     * Gets partitions from the client points based on the segments generated by
     * GetSegments. Partitions are used handle double-backed segments. Each
     * partition is a continuous sequence of points for a channel.
     *
     * @param segments
     * @param partitions OUT the partitions
     * @return
     */
    private static int GetPartitions(boolean[] segments,
            ArrayList<P1> partitions) {
        try {
            int j = 0;
            boolean nextSegment = false;
            //worst case is every segment is a separate partition
            //caller must deallocate partitions
            P1 p1 = new P1();
            //first segment will always be true,
            //there are no bad one-segment channels
            if (segments[0] == false) {
                return 0;
            }

            if (partitions != null) {
                partitions.clear();
            } else {
                return 0;
            }

            p1.start = 0;
            //only add the partitions after p1.end has been set
            int n = segments.length;
            //for (j = 0; j < segments.length - 1; j++) 
            for (j = 0; j < n - 1; j++) {
                nextSegment = segments[j + 1];
                if (nextSegment == false) {
                    //the end of the current partition is the last good segment
                    p1.end_Renamed = j;
                    partitions.add(p1);
                    //beginning of the next partition
                    p1 = new P1();
                    p1.start = j + 1;
                }
            }
            p1.end_Renamed = j;
            partitions.add(p1);
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            //clsUtility.WriteFile("error in clsChanneUtility.GetPartitions");
            ErrorLogger.LogException(_className, "GetPartitions",
                    new RendererException("Failed inside GetPartitions", exc));
        }
        return partitions.size();
    }

    /**
     * Draws a partition to the shapes array and stores the calculated channel
     * points
     *
     * @param fromSegment
     * @param toSegment
     * @param pixels
     * @param lineType
     * @param channelWidth
     * @param bolLastSegment
     * @param shapes
     * @param channelPoints
     * @param distanceToChannelPoint
     * @return
     */
    private static int DrawGoodChannel2(int fromSegment,
            int toSegment,
            double[] pixels,
            int lineType,
            int channelWidth,
            boolean bolLastSegment,
            ArrayList<Shape2> shapes,
            ArrayList<POINT2> channelPoints,
            double distanceToChannelPoint,
            int rev) {
        int returnValue = 0;	// Had to initialize to something
        try {
            int lineType2;
            double[] channelPixels = null;
            switch (lineType) {
                case TacticalLines.BELT1:
                    lineType2 = TacticalLines.BELT1;
                //break;
                case TacticalLines.LC:
                case TacticalLines.LC_HOSTILE:
                case TacticalLines.LC2:
                case TacticalLines.UNSP:
                case TacticalLines.DFENCE:
                case TacticalLines.SFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.BBS_LINE:
                case TacticalLines.SINGLEC:
                case TacticalLines.SINGLEC2:
                case TacticalLines.DOUBLEC:
                case TacticalLines.DOUBLEC2:
                case TacticalLines.TRIPLE:
                case TacticalLines.TRIPLE2:
                    lineType2 = lineType;
                    break;
                case TacticalLines.AAFNT:
                    if (fromSegment == 0) {
                        lineType2 = TacticalLines.CHANNEL_FLARED;
                    } else {
                        lineType2 = TacticalLines.CHANNEL;
                    }
                    break;
                case TacticalLines.SPT:
                    if (fromSegment == 0) {
                        lineType2 = TacticalLines.CHANNEL_FLARED;
                    } else {
                        lineType2 = TacticalLines.CHANNEL;
                    }
                    break;
                case TacticalLines.MAIN:
                    if (fromSegment == 0) {
                        lineType2 = TacticalLines.CHANNEL_FLARED;
                    } else {
                        lineType2 = TacticalLines.CHANNEL;
                    }
                    break;
                case TacticalLines.CATK:
                    lineType2 = TacticalLines.CHANNEL_DASHED;
                    break;
                case TacticalLines.CATKBYFIRE:
                    lineType2 = TacticalLines.CHANNEL_DASHED;
                    break;
                default:
                    lineType2 = TacticalLines.CHANNEL;
                    break;
            }
            if (bolLastSegment == true) {
                if (fromSegment != 0) {
                    switch (lineType) {
                        case TacticalLines.AAFNT:
                            lineType2 = TacticalLines.AAFNT_STRAIGHT;
                            break;
                        case TacticalLines.SPT:
                            lineType2 = TacticalLines.SPT_STRAIGHT;
                            break;
                        case TacticalLines.MAIN:
                            lineType2 = TacticalLines.MAIN_STRAIGHT;
                            break;
                        default:
                            lineType2 = (int) lineType;
                            break;
                    }
                } else {
                    lineType2 = (int) lineType;
                }
            }

            if (fromSegment < 0) {
                return returnValue;
            }
            if (toSegment < 0) {
                return returnValue;
            }
            if (toSegment < fromSegment) {
                return returnValue;
            }
            int j;
            int lineCount;
            int numPoints;
            int counter;
            double[] goodUpperPixels;
            double[] goodLowerPixels;
            numPoints = toSegment - fromSegment + 2;
            goodUpperPixels = new double[2 * numPoints];
            goodLowerPixels = new double[2 * numPoints];

            counter = 0;
            for (j = fromSegment; j < toSegment + 2; j++) {
                goodUpperPixels[counter] = pixels[2 * j];
                goodUpperPixels[counter + 1] = pixels[2 * j + 1];
                goodLowerPixels[counter] = pixels[2 * j];
                goodLowerPixels[counter + 1] = pixels[2 * j + 1];
                counter = counter + 2;
            }

            lineCount = CELineArray.CGetLineCountDouble(goodUpperPixels, numPoints, lineType2, channelWidth, rev);
            channelPixels = new double[3 * lineCount];
            POINT2 pt = null;
            lineCount = CELineArray.CGetChannel2Double(goodUpperPixels, goodLowerPixels, channelPixels, numPoints, numPoints, lineType2, channelWidth / 2, (int) distanceToChannelPoint, shapes, rev);

            //if shapes is null then it is not a CPOF client
            if (shapes == null && channelPixels != null) {
                //do not clear channelPoints first because the function gets successive calls
                int n = channelPixels.length;
                //for (j = 0; j < channelPixels.length / 3; j++) 
                for (j = 0; j < n / 3; j++) {
                    pt = new POINT2(channelPixels[3 * j], channelPixels[3 * j + 1], (int) channelPixels[3 * j + 2]);
                    if (j == channelPixels.length / 3 - 1) {
                        pt.style = 5;
                    }
                    channelPoints.add(pt);
                }
            }

            if (lineCount > 0) {
                //DrawChannelPixels2(lineCount, channelPixels, (int)lineType);
                returnValue = channelPixels.length;
            } else {
                returnValue = 0;
            }
            //channelPixels[channelPixels.length - 1] = 5;
            if (lineCount > 0) {
                channelPixels[lineCount - 1] = 5;
            }
            //clean up
            goodUpperPixels = null;
            goodLowerPixels = null;
        } catch (Exception exc) {
            //clsUtility.WriteFile("error in clsChanneUtility.DrawGoodChannel2");
            ErrorLogger.LogException(_className, "DrawGoodChannel2",
                    new RendererException("Failed inside DrawGoodChannel2", exc));
        }
        return returnValue;
    }

    /**
     * Draws the channel partitions to the shapes array
     *
     * @param pixels
     * @param partitions
     * @param linetype
     * @param channelWidth channel width in pixels
     * @param shapes
     * @param channelPoints
     * @param distanceToChannelPoint distance in pixels from the tip to the back
     * of the arrow
     */
    private static void DrawSegments(double[] pixels,
            ArrayList<P1> partitions,
            int linetype,
            int channelWidth,
            ArrayList<Shape2> shapes,
            ArrayList<POINT2> channelPoints,
            double distanceToChannelPoint,
            int rev) {
        try {
            int j = 0;
            int n = 0;
            int t = partitions.size();
            //for (j = 0; j < partitions.size() - 1; j++) 
            for (j = 0; j < t - 1; j++) {
                n = DrawGoodChannel2(partitions.get(j).start, partitions.get(j).end_Renamed, pixels, linetype, channelWidth, false, shapes, channelPoints, distanceToChannelPoint, rev);

            }
            //draw the last partition using linetype
            n = DrawGoodChannel2(partitions.get(j).start, partitions.get(j).end_Renamed, pixels, linetype, channelWidth, true, shapes, channelPoints, distanceToChannelPoint, rev);
        } catch (Exception exc) {
            //clsUtility.WriteFile("error in clsChanneUtility.DrawSegments");
            ErrorLogger.LogException(_className, "DrawSegments",
                    new RendererException("Failed inside DrawSegments", exc));
        }
    }

    /**
     * The main interface to clsChannelUtility calls DrawChannel2 after stuffing
     * the points into an array of 2-tuples x,y
     *
     * @param pixels the client points
     * @param linetype the line type
     * @param tg the tactical graphic
     * @param shapes
     * @param channelPoints
     * @param rev Mil-Standard-2525 revision
     */
    public static void DrawChannel(ArrayList<POINT2> pixels,
            int linetype,
            TGLight tg,
            ArrayList<Shape2> shapes,
            ArrayList<POINT2> channelPoints,
            int rev) {
        try {
            //we must do this because the rotary arrow tip now has to match the
            //anchor point, i.e. the rotary feature can no longer stick out past the anchor point
            //45 pixels shift here matches the 45 pixels shift for catkbyfire found in Channels.GetAXADDouble
            lineutility.adjustCATKBYFIREControlPoint(linetype, pixels, 45);
            if(tg.get_LineType()==TacticalLines.LC && tg.get_Affiliation().equalsIgnoreCase("H"))            
                linetype=TacticalLines.LC_HOSTILE;
            
            int j = 0;
            double[] pixels2 = new double[pixels.size() * 2];
            int n = pixels.size();
            //for (j = 0; j < pixels.size(); j++) 
            for (j = 0; j < n; j++) {
                pixels2[2 * j] = pixels.get(j).x;
                pixels2[2 * j + 1] = pixels.get(j).y;
            }
            DrawChannel2(pixels2, linetype, tg, shapes, channelPoints, rev);
        } catch (Exception exc) {
            //clsUtility.WriteFile("error in clsChanneUtility.DrawSegments");
            ErrorLogger.LogException(_className, "DrawChannel",
                    new RendererException("Failed inside DrawChannel", exc));
        }
    }

    /**
     * utility for clsMETOC to handle double-backed segments
     *
     * @param tg the tactical graphic object
     */
    public static ArrayList<P1> GetPartitions2(TGLight tg) {
        ArrayList partitions = null;
        try {
            double[] pixels = new double[tg.Pixels.size() * 2];
            int n = tg.Pixels.size();
            //for(int j=0;j<tg.Pixels.size();j++)
            for (int j = 0; j < n; j++) {
                pixels[2 * j] = tg.Pixels.get(j).x;
                pixels[2 * j + 1] = tg.Pixels.get(j).y;
            }

            boolean[] segments = new boolean[pixels.length / 2 - 1];
            if (segments.length == 0) {
                return null;
            }

            double factor = 3;

            clsUtility.GetSegments(pixels, segments, factor);
            partitions = new ArrayList<P1>();
            GetPartitions(segments, partitions);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetPartitions2",
                    new RendererException("Failed inside GetPartitions2", exc));
        }
        return partitions;
    }

    /**
     * The main function for processing channel types. Gets segments, then gets
     * partitions from the segments and then draws the partitions.
     *
     * @param pixels the client points as an array of 2-tuples x,y
     * @param linetype the line type
     * @param tg the tactical graphic object
     * @param shapes
     * @param channelPoints
     */
    private static void DrawChannel2(double[] pixels,
            int linetype,
            TGLight tg,
            ArrayList<Shape2> shapes,
            ArrayList<POINT2> channelPoints,
            int rev) {
        try {
            ref<double[]> distanceToChannelPoint = new ref();
            boolean bolAnimation = false;  //indicates if the colors are flipped from
            int j = 0;
            double[] pixels2 = null;
            int channelWidth = 0;
            ArrayList partitions = null;
            int n = pixels.length;
            int numPoints = 0;
            //LC and others do not call clsUtility.ChannelWidth, but the
            //value array still needs to be allocated or there is a
            //null pointer exception in DrawGoodChannel2
            distanceToChannelPoint.value = new double[1];
            distanceToChannelPoint.value[0] = 20;

            switch (linetype) {
                case TacticalLines.AAFNT:
                case TacticalLines.MAIN:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.AXAD:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.SPT:
                    clsUtility.ReorderPixels(pixels);
                    //diagnostic shorten last segment by channelwidth/4 for AAFNT
                    //so that the feint won't go past the 0th point
                    if (linetype == TacticalLines.AAFNT) {
                        channelWidth = clsUtility.ChannelWidth(pixels, distanceToChannelPoint) / 2;
                        POINT2 pt0 = new POINT2();
                        POINT2 pt1 = new POINT2();
                        pt0.x = pixels[pixels.length - 4];//-2
                        pt0.y = pixels[pixels.length - 3];//-1
                        pt1.x = pixels[pixels.length - 6];//-4
                        pt1.y = pixels[pixels.length - 5];//-3                                
                        pt0 = lineutility.ExtendAlongLineDouble(pt0, pt1, channelWidth / 8);
                        pixels[pixels.length - 4] = pt0.x;
                        pixels[pixels.length - 3] = pt0.y;
                    }
                    //end section
                    numPoints = pixels.length / 2;

                    if (numPoints < 3) {
                        return;
                    }
                    //moved this to be prior to stuffing pixels2
                    channelWidth = clsUtility.ChannelWidth(pixels, distanceToChannelPoint) / 2;
                    //ValidateChannelPixels2(ref pixels, ref channelWidth, ref distanceToChannelPoint);

                    numPoints = pixels.length / 2;
                    pixels2 = new double[pixels.length - 2];

                    for (j = 0; j < numPoints; j++) {
                        if (j < numPoints - 1) {
                            pixels2[2 * j] = pixels[2 * j];
                            pixels2[2 * j + 1] = pixels[2 * j + 1];
                        }
                    }
                    break;
                case TacticalLines.LC:
                case TacticalLines.LC_HOSTILE:
                case TacticalLines.LC2:
                    if (bolAnimation == true) {
                        channelWidth = 32;// was 16;
                    } else {
                        channelWidth = 40;// was 20;
                    }
                    pixels2 = new double[pixels.length];
                    n = pixels.length;
                    //for (j = 0; j < pixels.length; j++) 
                    for (j = 0; j < n; j++) {
                        pixels2[j] = pixels[j];
                    }
                    break;
                case TacticalLines.UNSP:
                case TacticalLines.DFENCE:
                case TacticalLines.SFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                    channelWidth = 30;  //was 20 1-10-13
                    if (Channels.getShiftLines()) {
                        channelWidth = 60;//was 40
                    }
                    pixels2 = new double[pixels.length];
                    n = pixels.length;
                    //for (j = 0; j < pixels.length; j++) 
                    for (j = 0; j < n; j++) {
                        pixels2[j] = pixels[j];
                    }
                    break;
                case TacticalLines.BBS_LINE:
                    channelWidth = 8 * tg.Pixels.get(0).style;  //was 20 1-10-13
                    pixels2 = new double[pixels.length];
                    n = pixels.length;
                    //for (j = 0; j < pixels.length; j++) 
                    for (j = 0; j < n; j++) {
                        pixels2[j] = pixels[j];
                    }
                    break;
                case TacticalLines.SINGLEC:    //7-9-07
                case TacticalLines.SINGLEC2:
                case TacticalLines.DOUBLEC:
                case TacticalLines.DOUBLEC2:
                case TacticalLines.TRIPLE:
                case TacticalLines.TRIPLE2:
                    //diagnostic 9-27-11
                    //clsUtility.MovePixels(pixels, pixels.length / 2);
                    channelWidth = 110;  //was 80
                    pixels2 = new double[pixels.length];
                    n = pixels.length;
                    //for (j = 0; j < pixels.length; j++) 
                    for (j = 0; j < n; j++) {
                        pixels2[j] = pixels[j];
                    }
                    break;
                default:

                    break;
            }

            //we require new partitions because pixels are dirty
            boolean[] segments = new boolean[pixels2.length / 2 - 1];
            if (segments.length == 0) {
                return;
            }

            double factor = 3;

            clsUtility.GetSegments(pixels2, segments, factor);
            partitions = new ArrayList();
            GetPartitions(segments, partitions);

            DrawSegments(pixels2, partitions, linetype, channelWidth, shapes, channelPoints, distanceToChannelPoint.value[0], rev);

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "DrawChannel2",
                    new RendererException("Failed inside DrawChannel2", exc));
        }
    }
}