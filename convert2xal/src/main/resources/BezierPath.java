/*
 * @(#)BezierPath.java  1.1  2006-03-22
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors ("JHotDraw.org")
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JHotDraw.org ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * JHotDraw.org.
 */

package org.jhotdraw.geom;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
/**
 * BezierPath allows the construction of paths consisting of straight lines,
 * quadratic curves and cubic curves.
 * <p>
 * A BezierPath represents a geometric path constructed by vertices.
 * Each Node has three control points: C0, C1, C2.
 * A mask defines which control points are in use. The path passes through
 * C0. C1 controls the curve going towards C0. C2 controls the curve going
 * away from C0.
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-03-22 Methods moveTo, lineTo and quadTo  added.
 * <br>1.0 January 20, 2006 Created.
 */
public class BezierPath extends ArrayList<BezierPath.Node>
        implements Shape {
    /** Constant for control point C1.
     * */
    public final static int C1_MASK = 1;
    /** Constant for control point C2. */
    public final static int C2_MASK = 2;
    /** Convenience constant for control point C1 and C2. */
    public final static int C1C2_MASK = C1_MASK | C2_MASK;
    
    /**
     * We cache a GeneralPath instance to speed up Shape operations.
     */
    private transient GeneralPath generalPath;
    
    /**
     * We cache the index of the outermost node to speed up method indexOfOutermostNode(); 
     */
    private int outer = -1;
    
    /**
     * If this value is set to true, closes the bezier path.
     */
    private boolean isClosed;
    
    /**
     * Defines a vertex (node) of the bezier path.
     * <p>
     * A vertex consists of three control points: C0, C1 and C2.
     * <ul>
     * <li>The bezier path always passes through C0.</li>
     * <li>C1 is used to control the curve towards C0.
     * </li>
     * <li>C2 is used to control the curve going away from C0.</li>
     * </ul>
     */
    
    /** Creates a new instance. */
    public BezierPath() {
    }
    

    /** Creates a deep copy of the BezierPath. */
    public Object clone() {
        try {
            Node that = (Node) super.clone();
            that.x = this.x.clone();
            that.y = this.y.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            InternalError error = new InternalError();
            error.initCause(e);
            throw error;
        }
    }
    

    
}