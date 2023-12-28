package com.example.slider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slider.ui.theme.SliderTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SliderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {

                            var currentValue by remember {
                                mutableIntStateOf(4)
                            }

                            Box(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Slider(range = 1..8, value = currentValue) {

                                }
                            }
                            /*
                            Button(onClick = { if( range == 8){
                                range -=1
                            }else{
                                range += 1
                            }
                            }) {

                            }

                             */
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Slider(
    modifier: Modifier = Modifier,
    range: IntRange,
    value: Int = range.first,
    onValueChange: (Int) -> Unit

) {
    Box {

        /*
        if(!range.contains(startValue)){
            throw IllegalArgumentException("Starting value must be within the specified intRange.")
        }

         */

        Column {
            var currentNumber by rememberSaveable {
                mutableIntStateOf(value)
            }
            val minRange = range.first
            val maxRange = range.last
            val totalRange = range.count()

            val knobRadius = 22.5f
            var targetPositionDifference by remember { mutableFloatStateOf(0.0f) }
            var popupKnobStaticPosition by remember {
                mutableFloatStateOf((140f))
            }
            var visible by remember {
                mutableStateOf(false)
            }
            var firstLoad by remember {
                mutableStateOf(true)
            }
            var currentPosition by remember {
                mutableFloatStateOf(140.0f)
            }


            val currentAnimatedPosition: Float by animateFloatAsState(
                targetValue = currentPosition,
                label = ""
            )


            var size by remember { mutableStateOf(IntSize.Zero) }

            val screenWidth = size.width.pxToDp().toString().replace(".dp", "").toFloat()
            var edgeSegmentWidth: Float
            var segmentWidth: Float

            Column(modifier = Modifier.onGloballyPositioned { coordinates ->
                if (coordinates.size.width != 0) {
                    size = coordinates.size
                }
            }) {

                val possibleValuesList by remember {
                    mutableStateOf(hashMapOf<Int, Float>())
                }

                LaunchedEffect(size) {

                    edgeSegmentWidth = (screenWidth / (totalRange)) / 2
                    segmentWidth = screenWidth / (totalRange)

                    var j = 1
                    for (i in minRange..maxRange) {
                        when (i) {
                            minRange -> {
                                possibleValuesList[i] = edgeSegmentWidth
                            }

                            totalRange -> {
                                possibleValuesList[i] =
                                    segmentWidth * j - segmentWidth + edgeSegmentWidth
                            }

                            else -> {
                                possibleValuesList[i] = segmentWidth * (j - 1) + edgeSegmentWidth
                            }
                        }
                        j++
                    }

                    currentPosition = possibleValuesList[currentNumber]!! - knobRadius
                }

                LaunchedEffect(value) {
                    if (possibleValuesList.keys.contains(value)) {
                        currentPosition = possibleValuesList[value]!! - knobRadius
                        currentNumber = value
                    }
                }

                PopupKnob(
                    currentPosition = if (!visible) popupKnobStaticPosition else currentPosition,
                    targetPositionDifference,
                    currentNumber,
                    visible
                )

                Box(
                    modifier = modifier.background(
                        color = Color.Transparent, shape = RoundedCornerShape(1.dp)
                    )
                ) {
                    SliderBackground {
                        Knob(
                            currentPosition = if (visible || firstLoad) currentPosition else currentAnimatedPosition,
                            onDrag = { deltaX ->
                                val tempEdgeSegmentWidth =
                                    possibleValuesList[possibleValuesList.keys.minOf { it }]
                                val tempSegmentWidth = tempEdgeSegmentWidth?.times(2)
                                val index =
                                    (((currentPosition + knobRadius - tempEdgeSegmentWidth!!) / tempSegmentWidth!!) % totalRange + 1).roundToInt()

                                val newNumber =
                                    possibleValuesList.keys.sorted().elementAt(index - 1)

                                if (newNumber != currentNumber) {
                                    currentNumber = newNumber
                                    onValueChange(newNumber)
                                }

                                //Check if position exceeds minimum position
                                if ((currentPosition + deltaX) < (possibleValuesList[possibleValuesList.keys.minOf { it }]!! - knobRadius)) {
                                    currentPosition =
                                        possibleValuesList[possibleValuesList.keys.minOf { it }]!! - knobRadius
                                    popupKnobStaticPosition =
                                        possibleValuesList[possibleValuesList.keys.minOf { it }]!! - knobRadius
                                }
                                //Check if position exceeds maximum position
                                else if ((currentPosition + deltaX) > possibleValuesList[possibleValuesList.keys.last()]!! - knobRadius) {
                                    currentPosition =
                                        possibleValuesList[possibleValuesList.keys.maxOf { it }]!! - knobRadius
                                    popupKnobStaticPosition =
                                        possibleValuesList[possibleValuesList.keys.maxOf { it }]!! - knobRadius
                                } else {
                                    currentPosition += deltaX
                                    popupKnobStaticPosition += deltaX
                                }

                            },
                            onDragEnd = {
                                popupKnobStaticPosition = currentPosition
                                currentPosition = possibleValuesList[currentNumber]!! - knobRadius
                                targetPositionDifference =
                                    if (currentPosition - popupKnobStaticPosition > 0) {
                                        (currentPosition - popupKnobStaticPosition) * 2 //+ knobRadius
                                    } else {
                                        (currentPosition - popupKnobStaticPosition) * 2 //- knobRadius
                                    }
                                firstLoad = false
                            },
                            onTap = {
                                targetPositionDifference = 0.0f
                                visible = it
                                popupKnobStaticPosition = currentPosition
                                firstLoad = false
                            }
                        )

                        Row {
                            for (i in minRange..maxRange) {
                                Text(
                                    text = (i).toString(),
                                    modifier = Modifier
                                        .weight(.05f)
                                    /*.clickable {
                                         currentNumber = i
                                         currentPosition = possibleValuesList[i]!! - 22.5f
                                     }*/,
                                    fontSize = (20).sp,
                                    color = if (currentNumber == i && !visible) Color.White else Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        OverLayKnob(currentPosition = currentPosition, isVisible = visible)
                    }
                }
            }
        }
    }
}


@Composable
fun SliderBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding()
            .height((50).dp)
            .background(
                color = Color(251, 152, 108), shape = RoundedCornerShape(30.dp)
            ), contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
fun Int.pxToDp() = with(LocalDensity.current) {
    this@pxToDp.toDp()
}


@Composable
fun Knob(
    currentPosition: Float, onDrag: (Float) -> Unit, onDragEnd: () -> Unit, onTap: (Boolean) -> Unit
) {
    Box(modifier = Modifier
        .offset(x = currentPosition.dp)
        .pointerInput(Unit) {
            detectTapGestures(onPress = {
                onTap(true)
                awaitRelease()
                onTap(false)
            })
        }
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { onTap(true) }, onDrag = { change, dragAmount ->
                change.consume()
                onDrag(dragAmount.x / 2.9f)
            }, onDragEnd = {
                onTap(false)
                onDragEnd()
            })
        }
        .size(45.dp)
        .background(
            color = Color(0xffCA6C42).copy(alpha = 1f), shape = RoundedCornerShape(30.dp)
        ))
}

@Composable
fun OverLayKnob(currentPosition: Float, isVisible: Boolean) {
    Box(
        modifier = Modifier
            .offset(x = currentPosition.dp)
            .size(45.dp)
            .background(
                color = if (isVisible) Color(0xffCA6C42).copy(alpha = 1f) else Color.Transparent,
                shape = RoundedCornerShape(30.dp)
            )
    )
}


@Composable
fun PopupKnob(currentPosition: Float, targetPosition: Float, currentNumber: Int, visible: Boolean) {
    Box {
        Spacer(modifier = Modifier.height(45.dp))
        AnimatedVisibility(visible = visible,
            enter = slideInVertically(
                initialOffsetY = { 100 }, animationSpec = spring(
                    stiffness = 1000f,
                    dampingRatio = 0.45f,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                )
            ),
            exit = slideOut(tween(500)) {
                IntOffset(targetPosition.roundToInt(), 135)
            }
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .offset(x = currentPosition.dp)
                    .background(color = Color(0xffFB986C), shape = RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = currentNumber.toString(), fontSize = 20.sp, color = Color.White)

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SliderPreview() {
    SliderTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(0.dp)) {
                Column {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Slider(range = 1..4) {

                        }
                    }
                }
            }
        }
    }
}