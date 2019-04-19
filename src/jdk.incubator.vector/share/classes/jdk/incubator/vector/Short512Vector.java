/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have
 * questions.
 */
package jdk.incubator.vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import jdk.internal.misc.Unsafe;
import jdk.internal.vm.annotation.ForceInline;
import static jdk.incubator.vector.VectorIntrinsics.*;

@SuppressWarnings("cast")
final class Short512Vector extends ShortVector {
    private static final VectorSpecies<Short> SPECIES = ShortVector.SPECIES_512;

    static final Short512Vector ZERO = new Short512Vector();

    static final int LENGTH = SPECIES.length();

    private final short[] vec; // Don't access directly, use getElements() instead.

    private short[] getElements() {
        return VectorIntrinsics.maybeRebox(this).vec;
    }

    Short512Vector() {
        vec = new short[SPECIES.length()];
    }

    Short512Vector(short[] v) {
        vec = v;
    }

    @Override
    public int length() { return LENGTH; }

    // Unary operator

    @Override
    Short512Vector uOp(FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector uOp(VectorMask<Short> o, FUnOp f) {
        short[] vec = getElements();
        short[] res = new short[length()];
        boolean[] mbits = ((Short512Mask)o).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec[i]) : vec[i];
        }
        return new Short512Vector(res);
    }

    // Binary operator

    @Override
    Short512Vector bOp(Vector<Short> o, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector bOp(Vector<Short> o1, VectorMask<Short> o2, FBinOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        boolean[] mbits = ((Short512Mask)o2).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i]) : vec1[i];
        }
        return new Short512Vector(res);
    }

    // Trinary operator

    @Override
    Short512Vector tOp(Vector<Short> o1, Vector<Short> o2, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = this.getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        for (int i = 0; i < length(); i++) {
            res[i] = f.apply(i, vec1[i], vec2[i], vec3[i]);
        }
        return new Short512Vector(res);
    }

    @Override
    Short512Vector tOp(Vector<Short> o1, Vector<Short> o2, VectorMask<Short> o3, FTriOp f) {
        short[] res = new short[length()];
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o1).getElements();
        short[] vec3 = ((Short512Vector)o2).getElements();
        boolean[] mbits = ((Short512Mask)o3).getBits();
        for (int i = 0; i < length(); i++) {
            res[i] = mbits[i] ? f.apply(i, vec1[i], vec2[i], vec3[i]) : vec1[i];
        }
        return new Short512Vector(res);
    }

    @Override
    short rOp(short v, FBinOp f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            v = f.apply(i, v, vec[i]);
        }
        return v;
    }

    @Override
    @ForceInline
    public <F> Vector<F> cast(VectorSpecies<F> s) {
        Objects.requireNonNull(s);
        if (s.length() != LENGTH)
            throw new IllegalArgumentException("Vector length this species length differ");

        return VectorIntrinsics.cast(
            Short512Vector.class,
            short.class, LENGTH,
            s.boxType(),
            s.elementType(), LENGTH,
            this, s,
            (species, vector) -> vector.castDefault(species)
        );
    }

    @SuppressWarnings("unchecked")
    @ForceInline
    private <F> Vector<F> castDefault(VectorSpecies<F> s) {
        int limit = s.length();

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
            byte[] a = new byte[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (byte) this.get(i);
            }
            return (Vector) ByteVector.fromArray((VectorSpecies<Byte>) s, a, 0);
        } else if (stype == short.class) {
            short[] a = new short[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (short) this.get(i);
            }
            return (Vector) ShortVector.fromArray((VectorSpecies<Short>) s, a, 0);
        } else if (stype == int.class) {
            int[] a = new int[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (int) this.get(i);
            }
            return (Vector) IntVector.fromArray((VectorSpecies<Integer>) s, a, 0);
        } else if (stype == long.class) {
            long[] a = new long[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (long) this.get(i);
            }
            return (Vector) LongVector.fromArray((VectorSpecies<Long>) s, a, 0);
        } else if (stype == float.class) {
            float[] a = new float[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (float) this.get(i);
            }
            return (Vector) FloatVector.fromArray((VectorSpecies<Float>) s, a, 0);
        } else if (stype == double.class) {
            double[] a = new double[limit];
            for (int i = 0; i < limit; i++) {
                a[i] = (double) this.get(i);
            }
            return (Vector) DoubleVector.fromArray((VectorSpecies<Double>) s, a, 0);
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    @SuppressWarnings("unchecked")
    public <F> Vector<F> reinterpret(VectorSpecies<F> s) {
        Objects.requireNonNull(s);

        if(s.elementType().equals(short.class)) {
            return (Vector<F>) reshape((VectorSpecies<Short>)s);
        }
        if(s.bitSize() == bitSize()) {
            return reinterpretType(s);
        }

        return defaultReinterpret(s);
    }

    @ForceInline
    private <F> Vector<F> reinterpretType(VectorSpecies<F> s) {
        Objects.requireNonNull(s);

        Class<?> stype = s.elementType();
        if (stype == byte.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Byte512Vector.class,
                byte.class, Byte512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == short.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Short512Vector.class,
                short.class, Short512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == int.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Int512Vector.class,
                int.class, Int512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == long.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Long512Vector.class,
                long.class, Long512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == float.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Float512Vector.class,
                float.class, Float512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else if (stype == double.class) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Double512Vector.class,
                double.class, Double512Vector.LENGTH,
                this, s,
                (species, vector) -> vector.defaultReinterpret(species)
            );
        } else {
            throw new UnsupportedOperationException("Bad lane type for casting.");
        }
    }

    @Override
    @ForceInline
    public ShortVector reshape(VectorSpecies<Short> s) {
        Objects.requireNonNull(s);
        if (s.bitSize() == 64 && (s.boxType() == Short64Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Short64Vector.class,
                short.class, Short64Vector.LENGTH,
                this, s,
                (species, vector) -> (ShortVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 128 && (s.boxType() == Short128Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Short128Vector.class,
                short.class, Short128Vector.LENGTH,
                this, s,
                (species, vector) -> (ShortVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 256 && (s.boxType() == Short256Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Short256Vector.class,
                short.class, Short256Vector.LENGTH,
                this, s,
                (species, vector) -> (ShortVector) vector.defaultReinterpret(species)
            );
        } else if (s.bitSize() == 512 && (s.boxType() == Short512Vector.class)) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                Short512Vector.class,
                short.class, Short512Vector.LENGTH,
                this, s,
                (species, vector) -> (ShortVector) vector.defaultReinterpret(species)
            );
        } else if ((s.bitSize() > 0) && (s.bitSize() <= 2048)
                && (s.bitSize() % 128 == 0) && (s.boxType() == ShortMaxVector.class)) {
            return VectorIntrinsics.reinterpret(
                Short512Vector.class,
                short.class, LENGTH,
                ShortMaxVector.class,
                short.class, ShortMaxVector.LENGTH,
                this, s,
                (species, vector) -> (ShortVector) vector.defaultReinterpret(species)
            );
        } else {
            throw new InternalError("Unimplemented size");
        }
    }

    // Binary operations with scalars

    @Override
    @ForceInline
    public ShortVector add(short o) {
        return add((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector add(short o, VectorMask<Short> m) {
        return add((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public ShortVector sub(short o) {
        return sub((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector sub(short o, VectorMask<Short> m) {
        return sub((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public ShortVector mul(short o) {
        return mul((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector mul(short o, VectorMask<Short> m) {
        return mul((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public ShortVector min(short o) {
        return min((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector max(short o) {
        return max((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> equal(short o) {
        return equal((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> notEqual(short o) {
        return notEqual((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> lessThan(short o) {
        return lessThan((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> lessThanEq(short o) {
        return lessThanEq((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> greaterThan(short o) {
        return greaterThan((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public VectorMask<Short> greaterThanEq(short o) {
        return greaterThanEq((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector blend(short o, VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }


    @Override
    @ForceInline
    public ShortVector and(short o) {
        return and((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector and(short o, VectorMask<Short> m) {
        return and((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public ShortVector or(short o) {
        return or((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector or(short o, VectorMask<Short> m) {
        return or((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public ShortVector xor(short o) {
        return xor((Short512Vector)ShortVector.broadcast(SPECIES, o));
    }

    @Override
    @ForceInline
    public ShortVector xor(short o, VectorMask<Short> m) {
        return xor((Short512Vector)ShortVector.broadcast(SPECIES, o), m);
    }

    @Override
    @ForceInline
    public Short512Vector neg() {
        return (Short512Vector)zero(SPECIES).sub(this);
    }

    // Unary operations

    @ForceInline
    @Override
    public Short512Vector neg(VectorMask<Short> m) {
        return blend(neg(), m);
    }

    @Override
    @ForceInline
    public Short512Vector abs() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_ABS, Short512Vector.class, short.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (short) Math.abs(a)));
    }

    @ForceInline
    @Override
    public Short512Vector abs(VectorMask<Short> m) {
        return blend(abs(), m);
    }


    @Override
    @ForceInline
    public Short512Vector not() {
        return VectorIntrinsics.unaryOp(
            VECTOR_OP_NOT, Short512Vector.class, short.class, LENGTH,
            this,
            v1 -> v1.uOp((i, a) -> (short) ~a));
    }

    @ForceInline
    @Override
    public Short512Vector not(VectorMask<Short> m) {
        return blend(not(), m);
    }
    // Binary operations

    @Override
    @ForceInline
    public Short512Vector add(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_ADD, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a + b)));
    }

    @Override
    @ForceInline
    public Short512Vector add(Vector<Short> v, VectorMask<Short> m) {
        return blend(add(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector sub(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_SUB, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a - b)));
    }

    @Override
    @ForceInline
    public Short512Vector sub(Vector<Short> v, VectorMask<Short> m) {
        return blend(sub(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector mul(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MUL, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a * b)));
    }

    @Override
    @ForceInline
    public Short512Vector mul(Vector<Short> v, VectorMask<Short> m) {
        return blend(mul(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector min(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return (Short512Vector) VectorIntrinsics.binaryOp(
            VECTOR_OP_MIN, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public Short512Vector min(Vector<Short> v, VectorMask<Short> m) {
        return blend(min(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector max(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_MAX, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short) Math.max(a, b)));
        }

    @Override
    @ForceInline
    public Short512Vector max(Vector<Short> v, VectorMask<Short> m) {
        return blend(max(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector and(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_AND, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a & b)));
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_OR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a | b)));
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;
        return VectorIntrinsics.binaryOp(
            VECTOR_OP_XOR, Short512Vector.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bOp(v2, (i, a, b) -> (short)(a ^ b)));
    }

    @Override
    @ForceInline
    public Short512Vector and(Vector<Short> v, VectorMask<Short> m) {
        return blend(and(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector or(Vector<Short> v, VectorMask<Short> m) {
        return blend(or(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector xor(Vector<Short> v, VectorMask<Short> m) {
        return blend(xor(v), m);
    }

    @Override
    @ForceInline
    public Short512Vector shiftL(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_LSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) (a << (i & 15))));
    }

    @Override
    @ForceInline
    public Short512Vector shiftL(int s, VectorMask<Short> m) {
        return blend(shiftL(s), m);
    }

    @Override
    @ForceInline
    public Short512Vector shiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_URSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) ((a & 0xFFFF) >>> (i & 15))));
    }

    @Override
    @ForceInline
    public Short512Vector shiftR(int s, VectorMask<Short> m) {
        return blend(shiftR(s), m);
    }

    @Override
    @ForceInline
    public Short512Vector aShiftR(int s) {
        return VectorIntrinsics.broadcastInt(
            VECTOR_OP_RSHIFT, Short512Vector.class, short.class, LENGTH,
            this, s,
            (v, i) -> v.uOp((__, a) -> (short) (a >> (i & 15))));
    }

    @Override
    @ForceInline
    public Short512Vector aShiftR(int s, VectorMask<Short> m) {
        return blend(aShiftR(s), m);
    }
    // Ternary operations


    // Type specific horizontal reductions

    @Override
    @ForceInline
    public short addAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_ADD, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a + b)));
    }

    @Override
    @ForceInline
    public short andAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_AND, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) -1, (i, a, b) -> (short) (a & b)));
    }

    @Override
    @ForceInline
    public short andAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, (short) -1), m).andAll();
    }

    @Override
    @ForceInline
    public short minAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MIN, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp(Short.MAX_VALUE , (i, a, b) -> (short) Math.min(a, b)));
    }

    @Override
    @ForceInline
    public short maxAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MAX, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp(Short.MIN_VALUE , (i, a, b) -> (short) Math.max(a, b)));
    }

    @Override
    @ForceInline
    public short mulAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_MUL, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 1, (i, a, b) -> (short) (a * b)));
    }

    @Override
    @ForceInline
    public short orAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_OR, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a | b)));
    }

    @Override
    @ForceInline
    public short orAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, (short) 0), m).orAll();
    }

    @Override
    @ForceInline
    public short xorAll() {
        return (short) VectorIntrinsics.reductionCoerced(
            VECTOR_OP_XOR, Short512Vector.class, short.class, LENGTH,
            this,
            v -> (long) v.rOp((short) 0, (i, a, b) -> (short) (a ^ b)));
    }

    @Override
    @ForceInline
    public short xorAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, (short) 0), m).xorAll();
    }


    @Override
    @ForceInline
    public short addAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, (short) 0), m).addAll();
    }


    @Override
    @ForceInline
    public short mulAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, (short) 1), m).mulAll();
    }

    @Override
    @ForceInline
    public short minAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, Short.MAX_VALUE), m).minAll();
    }

    @Override
    @ForceInline
    public short maxAll(VectorMask<Short> m) {
        return blend((Short512Vector)ShortVector.broadcast(SPECIES, Short.MIN_VALUE), m).maxAll();
    }

    @Override
    @ForceInline
    public VectorShuffle<Short> toShuffle() {
        short[] a = toArray();
        int[] sa = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            sa[i] = (int) a[i];
        }
        return VectorShuffle.fromArray(SPECIES, sa, 0);
    }

    // Memory operations

    private static final int ARRAY_SHIFT         = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_SHORT_INDEX_SCALE);
    private static final int BOOLEAN_ARRAY_SHIFT = 31 - Integer.numberOfLeadingZeros(Unsafe.ARRAY_BOOLEAN_INDEX_SCALE);

    @Override
    @ForceInline
    public void intoArray(short[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, LENGTH);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               a, (((long) ix) << ARRAY_SHIFT) + Unsafe.ARRAY_SHORT_BASE_OFFSET,
                               this,
                               a, ix,
                               (arr, idx, v) -> v.forEach((i, e) -> arr[idx + i] = e));
    }

    @Override
    @ForceInline
    public final void intoArray(short[] a, int ax, VectorMask<Short> m) {
        ShortVector oldVal = ShortVector.fromArray(SPECIES, a, ax);
        ShortVector newVal = oldVal.blend(this, m);
        newVal.intoArray(a, ax);
    }

    @Override
    @ForceInline
    public void intoByteArray(byte[] a, int ix) {
        Objects.requireNonNull(a);
        ix = VectorIntrinsics.checkIndex(ix, a.length, bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               a, ((long) ix) + Unsafe.ARRAY_BYTE_BASE_OFFSET,
                               this,
                               a, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = ByteBuffer.wrap(c, idx, c.length - idx).order(ByteOrder.nativeOrder());
                                   ShortBuffer tb = bbc.asShortBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public final void intoByteArray(byte[] a, int ix, VectorMask<Short> m) {
        Short512Vector oldVal = (Short512Vector) ShortVector.fromByteArray(SPECIES, a, ix);
        Short512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteArray(a, ix);
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix) {
        if (bb.order() != ByteOrder.nativeOrder()) {
            throw new IllegalArgumentException();
        }
        if (bb.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        ix = VectorIntrinsics.checkIndex(ix, bb.limit(), bitSize() / Byte.SIZE);
        VectorIntrinsics.store(Short512Vector.class, short.class, LENGTH,
                               U.getReference(bb, BYTE_BUFFER_HB), ix + U.getLong(bb, BUFFER_ADDRESS),
                               this,
                               bb, ix,
                               (c, idx, v) -> {
                                   ByteBuffer bbc = c.duplicate().position(idx).order(ByteOrder.nativeOrder());
                                   ShortBuffer tb = bbc.asShortBuffer();
                                   v.forEach((i, e) -> tb.put(e));
                               });
    }

    @Override
    @ForceInline
    public void intoByteBuffer(ByteBuffer bb, int ix, VectorMask<Short> m) {
        Short512Vector oldVal = (Short512Vector) ShortVector.fromByteBuffer(SPECIES, bb, ix);
        Short512Vector newVal = oldVal.blend(this, m);
        newVal.intoByteBuffer(bb, ix);
    }

    //

    @Override
    public String toString() {
        return Arrays.toString(getElements());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Short512Vector that = (Short512Vector) o;
        return this.equal(that).allTrue();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vec);
    }

    // Binary test

    @Override
    Short512Mask bTest(Vector<Short> o, FBinTest f) {
        short[] vec1 = getElements();
        short[] vec2 = ((Short512Vector)o).getElements();
        boolean[] bits = new boolean[length()];
        for (int i = 0; i < length(); i++){
            bits[i] = f.apply(i, vec1[i], vec2[i]);
        }
        return new Short512Mask(bits);
    }

    // Comparisons

    @Override
    @ForceInline
    public Short512Mask equal(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_eq, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a == b));
    }

    @Override
    @ForceInline
    public Short512Mask notEqual(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_ne, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a != b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThan(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_lt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a < b));
    }

    @Override
    @ForceInline
    public Short512Mask lessThanEq(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_le, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a <= b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThan(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return (Short512Mask) VectorIntrinsics.compare(
            BT_gt, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a > b));
    }

    @Override
    @ForceInline
    public Short512Mask greaterThanEq(Vector<Short> o) {
        Objects.requireNonNull(o);
        Short512Vector v = (Short512Vector)o;

        return VectorIntrinsics.compare(
            BT_ge, Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v,
            (v1, v2) -> v1.bTest(v2, (i, a, b) -> a >= b));
    }

    // Foreach

    @Override
    void forEach(FUnCon f) {
        short[] vec = getElements();
        for (int i = 0; i < length(); i++) {
            f.apply(i, vec[i]);
        }
    }

    @Override
    void forEach(VectorMask<Short> o, FUnCon f) {
        boolean[] mbits = ((Short512Mask)o).getBits();
        forEach((i, a) -> {
            if (mbits[i]) { f.apply(i, a); }
        });
    }



    @Override
    public Short512Vector rotateEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            res[(j + i) % length()] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector rotateER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length(); i++){
            int z = i - j;
            if(j < 0) {
                res[length() + z] = vec[i];
            } else {
                res[z] = vec[i];
            }
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftEL(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++) {
            res[i] = vec[i + j];
        }
        return new Short512Vector(res);
    }

    @Override
    public Short512Vector shiftER(int j) {
        short[] vec = getElements();
        short[] res = new short[length()];
        for (int i = 0; i < length() - j; i++){
            res[i + j] = vec[i];
        }
        return new Short512Vector(res);
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(Vector<Short> v,
                                  VectorShuffle<Short> s, VectorMask<Short> m) {
        return this.rearrange(s).blend(v.rearrange(s), m);
    }

    @Override
    @ForceInline
    public Short512Vector rearrange(VectorShuffle<Short> o1) {
        Objects.requireNonNull(o1);
        Short512Shuffle s =  (Short512Shuffle)o1;

        return VectorIntrinsics.rearrangeOp(
            Short512Vector.class, Short512Shuffle.class, short.class, LENGTH,
            this, s,
            (v1, s_) -> v1.uOp((i, a) -> {
                int ei = s_.getElement(i);
                return v1.get(ei);
            }));
    }

    @Override
    @ForceInline
    public Short512Vector blend(Vector<Short> o1, VectorMask<Short> o2) {
        Objects.requireNonNull(o1);
        Objects.requireNonNull(o2);
        Short512Vector v = (Short512Vector)o1;
        Short512Mask   m = (Short512Mask)o2;

        return VectorIntrinsics.blend(
            Short512Vector.class, Short512Mask.class, short.class, LENGTH,
            this, v, m,
            (v1, v2, m_) -> v1.bOp(v2, (i, a, b) -> m_.getElement(i) ? b : a));
    }

    // Accessors

    @Override
    public short get(int i) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return (short) VectorIntrinsics.extract(
                                Short512Vector.class, short.class, LENGTH,
                                this, i,
                                (vec, ix) -> {
                                    short[] vecarr = vec.getElements();
                                    return (long)vecarr[ix];
                                });
    }

    @Override
    public Short512Vector with(int i, short e) {
        if (i < 0 || i >= LENGTH) {
            throw new IllegalArgumentException("Index " + i + " must be zero or positive, and less than " + LENGTH);
        }
        return VectorIntrinsics.insert(
                                Short512Vector.class, short.class, LENGTH,
                                this, i, (long)e,
                                (v, ix, bits) -> {
                                    short[] res = v.getElements().clone();
                                    res[ix] = (short)bits;
                                    return new Short512Vector(res);
                                });
    }

    // Mask

    static final class Short512Mask extends AbstractMask<Short> {
        static final Short512Mask TRUE_MASK = new Short512Mask(true);
        static final Short512Mask FALSE_MASK = new Short512Mask(false);

        private final boolean[] bits; // Don't access directly, use getBits() instead.

        public Short512Mask(boolean[] bits) {
            this(bits, 0);
        }

        public Short512Mask(boolean[] bits, int offset) {
            boolean[] a = new boolean[species().length()];
            for (int i = 0; i < a.length; i++) {
                a[i] = bits[offset + i];
            }
            this.bits = a;
        }

        public Short512Mask(boolean val) {
            boolean[] bits = new boolean[species().length()];
            Arrays.fill(bits, val);
            this.bits = bits;
        }

        boolean[] getBits() {
            return VectorIntrinsics.maybeRebox(this).bits;
        }

        @Override
        Short512Mask uOp(MUnOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        Short512Mask bOp(VectorMask<Short> o, MBinOp f) {
            boolean[] res = new boolean[species().length()];
            boolean[] bits = getBits();
            boolean[] mbits = ((Short512Mask)o).getBits();
            for (int i = 0; i < species().length(); i++) {
                res[i] = f.apply(i, bits[i], mbits[i]);
            }
            return new Short512Mask(res);
        }

        @Override
        public VectorSpecies<Short> species() {
            return SPECIES;
        }

        @Override
        public Short512Vector toVector() {
            short[] res = new short[species().length()];
            boolean[] bits = getBits();
            for (int i = 0; i < species().length(); i++) {
                // -1 will result in the most significant bit being set in
                // addition to some or all other bits
                res[i] = (short) (bits[i] ? -1 : 0);
            }
            return new Short512Vector(res);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <E> VectorMask<E> cast(VectorSpecies<E> species) {
            if (length() != species.length())
                throw new IllegalArgumentException("VectorMask length and species length differ");
            Class<?> stype = species.elementType();
            boolean [] maskArray = toArray();
            if (stype == byte.class) {
                return (VectorMask <E>) new Byte512Vector.Byte512Mask(maskArray);
            } else if (stype == short.class) {
                return (VectorMask <E>) new Short512Vector.Short512Mask(maskArray);
            } else if (stype == int.class) {
                return (VectorMask <E>) new Int512Vector.Int512Mask(maskArray);
            } else if (stype == long.class) {
                return (VectorMask <E>) new Long512Vector.Long512Mask(maskArray);
            } else if (stype == float.class) {
                return (VectorMask <E>) new Float512Vector.Float512Mask(maskArray);
            } else if (stype == double.class) {
                return (VectorMask <E>) new Double512Vector.Double512Mask(maskArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        // Unary operations

        @Override
        @ForceInline
        public Short512Mask not() {
            return (Short512Mask) VectorIntrinsics.unaryOp(
                                             VECTOR_OP_NOT, Short512Mask.class, short.class, LENGTH,
                                             this,
                                             (m1) -> m1.uOp((i, a) -> !a));
        }

        // Binary operations

        @Override
        @ForceInline
        public Short512Mask and(VectorMask<Short> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_AND, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a & b));
        }

        @Override
        @ForceInline
        public Short512Mask or(VectorMask<Short> o) {
            Objects.requireNonNull(o);
            Short512Mask m = (Short512Mask)o;
            return VectorIntrinsics.binaryOp(VECTOR_OP_OR, Short512Mask.class, short.class, LENGTH,
                                             this, m,
                                             (m1, m2) -> m1.bOp(m2, (i, a, b) -> a | b));
        }

        // Reductions

        @Override
        @ForceInline
        public boolean anyTrue() {
            return VectorIntrinsics.test(BT_ne, Short512Mask.class, short.class, LENGTH,
                                         this, this,
                                         (m, __) -> anyTrueHelper(((Short512Mask)m).getBits()));
        }

        @Override
        @ForceInline
        public boolean allTrue() {
            return VectorIntrinsics.test(BT_overflow, Short512Mask.class, short.class, LENGTH,
                                         this, VectorMask.maskAllTrue(species()),
                                         (m, __) -> allTrueHelper(((Short512Mask)m).getBits()));
        }
    }

    // Shuffle

    static final class Short512Shuffle extends AbstractShuffle<Short> {
        Short512Shuffle(byte[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder) {
            super(reorder);
        }

        public Short512Shuffle(int[] reorder, int i) {
            super(reorder, i);
        }

        public Short512Shuffle(IntUnaryOperator f) {
            super(f);
        }

        @Override
        public VectorSpecies<Short> species() {
            return SPECIES;
        }

        @Override
        public ShortVector toVector() {
            short[] va = new short[SPECIES.length()];
            for (int i = 0; i < va.length; i++) {
              va[i] = (short) getElement(i);
            }
            return ShortVector.fromArray(SPECIES, va, 0);
        }

        @Override
        @ForceInline
        @SuppressWarnings("unchecked")
        public <F> VectorShuffle<F> cast(VectorSpecies<F> species) {
            if (length() != species.length())
                throw new IllegalArgumentException("Shuffle length and species length differ");
            Class<?> stype = species.elementType();
            int [] shuffleArray = toArray();
            if (stype == byte.class) {
                return (VectorShuffle<F>) new Byte512Vector.Byte512Shuffle(shuffleArray);
            } else if (stype == short.class) {
                return (VectorShuffle<F>) new Short512Vector.Short512Shuffle(shuffleArray);
            } else if (stype == int.class) {
                return (VectorShuffle<F>) new Int512Vector.Int512Shuffle(shuffleArray);
            } else if (stype == long.class) {
                return (VectorShuffle<F>) new Long512Vector.Long512Shuffle(shuffleArray);
            } else if (stype == float.class) {
                return (VectorShuffle<F>) new Float512Vector.Float512Shuffle(shuffleArray);
            } else if (stype == double.class) {
                return (VectorShuffle<F>) new Double512Vector.Double512Shuffle(shuffleArray);
            } else {
                throw new UnsupportedOperationException("Bad lane type for casting.");
            }
        }

        @Override
        public Short512Shuffle rearrange(VectorShuffle<Short> o) {
            Short512Shuffle s = (Short512Shuffle) o;
            byte[] r = new byte[reorder.length];
            for (int i = 0; i < reorder.length; i++) {
                r[i] = reorder[s.reorder[i]];
            }
            return new Short512Shuffle(r);
        }
    }

    // VectorSpecies

    @Override
    public VectorSpecies<Short> species() {
        return SPECIES;
    }
}
